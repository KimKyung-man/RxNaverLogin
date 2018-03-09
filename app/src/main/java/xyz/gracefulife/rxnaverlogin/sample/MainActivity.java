package xyz.gracefulife.rxnaverlogin.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import xyz.gracefulife.rxnaverlogin.library.NaverApiKeyProvider;
import xyz.gracefulife.rxnaverlogin.library.Response;
import xyz.gracefulife.rxnaverlogin.library.RxNaverLogin;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();
  private CompositeDisposable compositeDisposable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    compositeDisposable = new CompositeDisposable();
    findViewById(R.id.button_naver).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        RxNaverLogin rxNaverLogin = RxNaverLogin.with(
            MainActivity.this,
            NaverApiKeyProvider
                .of("put your CLIENT_ID", "put your CLIENT_SECRET", "put your CLIENT_NAME")
        );

        rxNaverLogin.waitLoggedIn()
            .subscribe(new SingleObserver<Response>() {
              @Override public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
              }

              @Override public void onSuccess(Response response) {
                Log.i(TAG, "onSuccess: success = " + response);
              }

              @Override public void onError(Throwable e) {
                e.printStackTrace();
                Log.i(TAG, "onError: ....");
              }
            });

        rxNaverLogin.doOAuthLogin();
      }
    });
  }
}
