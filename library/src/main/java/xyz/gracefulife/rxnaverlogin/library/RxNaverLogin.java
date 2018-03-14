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
   * 네.아.로에서 직접 사용한다면 이 핸들러를 이용한다.
   */
  public NaverOAuthLoginHandler getNaverOAuthLoginHandler() {
    return naverOAuthLoginHandler;
  }

  /**
   * 네.아.로에서 제공하는 로그인이 아닌 직접 로그인을 시도하는 경우
   */
  public void doOAuthLogin() {
    oAuthLogin.startOauthLoginActivity(activity, naverOAuthLoginHandler);
  }

  /**
   * 로그인 이벤트를 한번 기다린다.
   * single 타입을 리턴하므로, 재사용 시 다시 호출하거나,
   * 이벤트를 계속 받아야 한다면 getNaverOAuthLoginHandler 메소드를 직접 사용하여,
   * eventbus를 직접 구독할 수 있다.
   */
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
