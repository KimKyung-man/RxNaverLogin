package xyz.gracefulife.rxnaverlogin.library;

/**
 * Created by grace on 2018-03-08.
 */

public class NaverApiKeyProvider {
  private String clientId;
  private String clientSecret;
  private String clientName;

  private NaverApiKeyProvider() {
  }

  public static NaverApiKeyProvider of(String clientId, String clientSecret, String clientName) {
    NaverApiKeyProvider naverApiKeyProvider = new NaverApiKeyProvider();
    naverApiKeyProvider.clientId = clientId;
    naverApiKeyProvider.clientSecret = clientSecret;
    naverApiKeyProvider.clientName = clientName;
    return naverApiKeyProvider;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getClientName() {
    return clientName;
  }
}
