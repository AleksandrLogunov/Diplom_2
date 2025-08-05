package user;

import client.UserClient;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import model.LoginCredentials;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;

public class LoginUserTest {

    UserClient userClient;
    User testUser;
    String accessTokenToDelete;

    @BeforeEach
    @Step("Настройка перед тестом: создание и регистрация тестового пользователя")
    void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        userClient = new UserClient();
        testUser = User.getRandomUser();
        userClient.createUser(testUser)
                .then().statusCode(200);

        LoginCredentials creds = new LoginCredentials(testUser.getEmail(), testUser.getPassword());
        accessTokenToDelete = userClient.loginUser(creds).then().extract().path("accessToken");
    }

    @AfterEach
    @Step("Удаление тестового пользователя после теста")
    void tearDown() {
        if (accessTokenToDelete != null) {
            userClient.deleteUser(testUser)
                    .then()
                    .statusCode(202);
        }
    }

    @Test
    @DisplayName("Успешный логин под существующим пользователем")
    void loginExistingUserSuccessfully() {
        LoginCredentials creds = new LoginCredentials(testUser.getEmail(), testUser.getPassword());
        userClient.loginUser(creds)
                .then().statusCode(200)
                .body("success", is(true))
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Логин с неверным email")
    void loginWithWrongEmailFails() {
        LoginCredentials creds = new LoginCredentials("wrong" + testUser.getEmail(), testUser.getPassword());
        userClient.loginUser(creds)
                .then().statusCode(401)
                .body("message", is("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин с неверным паролем")
    void loginWithWrongPasswordFails() {
        LoginCredentials creds = new LoginCredentials(testUser.getEmail(), "wrong" + testUser.getPassword());
        userClient.loginUser(creds)
                .then().statusCode(401)
                .body("message", is("email or password are incorrect"));
    }
}