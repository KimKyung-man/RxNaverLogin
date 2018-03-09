package xyz.gracefulife.rxnaverlogin.library;

import android.app.Activity;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;

import java.lang.ref.WeakReference;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by grace on 2018-03-07.
 */

public class RxNaverLogin {
  private static final String TAG = RxNaverLogin.class.getSimpleName();

  private NaverApiKeyProvider naverApiKeyProvider;

  private Activity activity;
  private NaverOAuthLoginHandler naverOAuthLoginHandler;

  private PublishSubject<Boolean> successEventBus;
  private OAuthLogin oAuthLogin;

  private RxNaverLogin(@NonNull NaverApiKeyProvider naverApiKeyProvider) {
    this.naverApiKeyProvider = naverApiKeyProvider;
  }

  public static RxNaverLogin with(
      @NonNull Activity activity, @NonNull NaverApiKeyProvider naverApiKeyProvider) {
    RxNaverLogin rxNaverLogin = new RxNaverLogin(naverApiKeyProvider);
    rxNaverLogin.activity = activity;

    rxNaverLogin.successEventBus = PublishSubject.create();

    rxNaverLogin.oAuthLogin = OAuthLogin.getInstance();
    rxNaverLogin.oAuthLogin.showDevelopersLog(true);
    rxNaverLogin.oAuthLogin.init(activity, naverApiKeyProvider.getClientId(),
        naverApiKeyProvider.getClientSecret(), naverApiKeyProvider.getClientName());

    rxNaverLogin.naverOAuthLoginHandler =
        new NaverOAuthLoginHandler(activity, rxNaverLogin.successEventBus);
    return rxNaverLogin;
  }

  /**
   * 네.아.로에서 제공하는 로그인이 아닌 직접 로그인을 시도하는 경우
   */
  public void doOAuthLogin() {
    oAuthLogin.startOauthLoginActivity(activity, naverOAuthLoginHandler);
  }

  public Single<Response> waitLoggedIn() {
    return naverOAuthLoginHandler.getSuccessEventBus()
        .observeOn(Schedulers.io())
        .flatMapSingle(new Function<Boolean, Single<Response>>() {
          @Override public Single<Response> apply(final Boolean aBoolean) throws Exception {
            return Single.create(new SingleOnSubscribe<Response>() {
              @Override public void subscribe(SingleEmitter<Response> e) throws Exception {
                if (!aBoolean) {
                  e.onError(
                      new IllegalArgumentException(oAuthLogin.getLastErrorCode(activity) + " / "
                          + oAuthLogin.getLastErrorDesc(activity))
                  );
                  return;
                }

                String url = "https://openapi.naver.com/v1/nid/me";
                String accessToken = oAuthLogin.getAccessToken(activity);

                String responseJson = oAuthLogin.requestApi(activity, accessToken, url);
                Gson gson = new Gson();
                e.onSuccess(gson.fromJson(responseJson, Response.class));
              }
            });
          }
        })
        .firstOrError();
  }


  public static final class NaverOAuthLoginHandler extends OAuthLoginHandler {
    private final WeakReference<Activity> ref;

    private final PublishSubject<Boolean> successEventBus;

    public NaverOAuthLoginHandler(Activity activity, PublishSubject<Boolean> successEventBus) {
      this.ref = new WeakReference<>(activity);
      this.successEventBus = successEventBus;
    }

    public PublishSubject<Boolean> getSuccessEventBus() {
      return successEventBus;
    }

    @Override
    public void handleMessage(Message msg) {
      Activity act = ref.get();

      // lost reference
      if (act == null) {
        return;
      }
      Log.i(TAG, "handleMessage: msg = " + msg);
      this.run(msg.what == 1);
    }

    @Override public void run(boolean success) {
      try {
        successEventBus.onNext(success);
      } catch (Exception e) {
        successEventBus.onError(e);
        e.printStackTrace();
      }
    }

  }
}
