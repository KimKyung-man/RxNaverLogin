package xyz.gracefulife.rxnaverlogin.library;

/**
 * Created by grace on 2018-03-09.
 */

public class Response {
  private String resultcode;
  private String message;
  private Data response;

  public String getResultcode() {
    return resultcode;
  }

  public void setResultcode(String resultcode) {
    this.resultcode = resultcode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Data getResponse() {
    return response;
  }

  public void setResponse(Data response) {
    this.response = response;
  }

  @Override public String toString() {
    return "Response{" +
        "resultcode='" + resultcode + '\'' +
        ", message='" + message + '\'' +
        ", response=" + response +
        '}';
  }

  public static class Data {
    private String id;
    private String name;
    private String email;
    private String gender;
    private String age;
    private String birthday;
    private String profile_image;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getGender() {
      return gender;
    }

    public void setGender(String gender) {
      this.gender = gender;
    }

    public String getAge() {
      return age;
    }

    public void setAge(String age) {
      this.age = age;
    }

    public String getBirthday() {
      return birthday;
    }

    public void setBirthday(String birthday) {
      this.birthday = birthday;
    }

    public String getProfile_image() {
      return profile_image;
    }

    public void setProfile_image(String profile_image) {
      this.profile_image = profile_image;
    }

    @Override public String toString() {
      return "Data{" +
          "id='" + id + '\'' +
          ", name='" + name + '\'' +
          ", email='" + email + '\'' +
          ", gender='" + gender + '\'' +
          ", age='" + age + '\'' +
          ", birthday='" + birthday + '\'' +
          ", profile_image='" + profile_image + '\'' +
          '}';
    }
  }
}
