package client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.LoginCredentials;
import model.User;
import static io.restassured.RestAssured.given;

public class UserClient {

    private final String BASE_PATH = "/api/auth";

    @Step("Создание пользователя")
    public Response createUser(User user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .post(BASE_PATH + "/register");
    }

    @Step("Логин пользователя")
    public Response loginUser(LoginCredentials creds) {
        return given()
                .header("Content-type", "application/json")
                .body(creds)
                .post(BASE_PATH + "/login");
    }

    @Step("Обновление пользователя")
    public Response updateUser(User user, String token) {
        var request = given()
                .header("Content-type", "application/json");

        if (token != null && !token.isEmpty()) {
            request.header("Authorization", token);
        }

        return request
                .body(user)
                .patch(BASE_PATH + "/user");
    }

    @Step("Удаление пользователя")
    public Response deleteUser(User user) {
        LoginCredentials creds = new LoginCredentials(user.getEmail(), user.getPassword());
        Response login = loginUser(creds);
        String token = login.then().extract().path("accessToken");

        if (token != null) {
            return given()
                    .header("Authorization", token)
                    .delete(BASE_PATH + "/user");
        } else {
            return login;
        }
    }
}
