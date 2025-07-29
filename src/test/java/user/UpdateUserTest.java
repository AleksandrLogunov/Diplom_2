package user;

import client.UserClient;
import io.restassured.RestAssured;
import model.LoginCredentials;
import model.User;
import org.junit.jupiter.api.*;
import static org.hamcrest.Matchers.*;

public class UpdateUserTest {

    UserClient userClient;
    User testUser;
    String token;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        userClient = new UserClient();
        testUser = User.getRandomUser();
        userClient.createUser(testUser);
        token = userClient.loginUser(
                new model.LoginCredentials(testUser.getEmail(), testUser.getPassword())
        ).then().extract().path("accessToken");
    }

    @Test
    @DisplayName("Обновление имени с авторизацией")
    void updateNameWithAuth() {
        testUser.setName("UpdatedName");
        userClient.updateUser(testUser, token)
                .then().statusCode(200)
                .body("user.name", equalTo("UpdatedName"));
    }

    @Test
    @DisplayName("Обновление email с авторизацией")
    void updateEmailWithAuth() {
        String newEmail = "new_email_" + System.currentTimeMillis() + "@mail.ru";
        testUser.setEmail(newEmail);

        userClient.updateUser(testUser, token)
                .then().statusCode(200)
                .body("success", is(true))
                .body("user.email", equalTo(newEmail));
    }

    @Test
    @DisplayName("Попытка обновления email на уже занятый email")
    void updateEmailToAlreadyExistingEmailFails() {
        User conflictingUser = User.getRandomUser();
        userClient.createUser(conflictingUser)
                .then().statusCode(200);

        testUser.setEmail(conflictingUser.getEmail());

        userClient.updateUser(testUser, token)
                .then().statusCode(403)
                .body("success", is(false))
                .body("message", is("User with such email already exists"));
    }

    @Test
    @DisplayName("Обновление пароля с авторизацией")
    void updatePasswordWithAuth() {
        String newPassword = "new_password_" + System.currentTimeMillis();
        testUser.setPassword(newPassword);

        userClient.updateUser(testUser, token)
                .then().statusCode(200)
                .body("success", is(true))
                .body("user.email", equalTo(testUser.getEmail()))
                .body("user.name", equalTo(testUser.getName()));

        LoginCredentials newCreds = new LoginCredentials(testUser.getEmail(), newPassword);
        userClient.loginUser(newCreds)
                .then().statusCode(200)
                .body("success", is(true))
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Попытка обновления имени без авторизации")
    void updateNameWithoutAuthFails() {
        testUser.setName("UnauthorizedName");
        userClient.updateUser(testUser, null)
                .then().statusCode(401)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @Test
    @DisplayName("Обновление email без авторизации")
    void updateEmailWithoutAuth() {
        testUser.setEmail("newemail@mail.ru");
        userClient.updateUser(testUser, null)
                .then().statusCode(401)
                .body("message", is("You should be authorised"));
    }

    @Test
    @DisplayName("Попытка обновления пароля без авторизации")
    void updatePasswordWithoutAuthFails() {
        testUser.setPassword("unauthorized_new_password");
        userClient.updateUser(testUser, null)
                .then().statusCode(401)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @AfterEach
    void tearDown() {
        userClient.deleteUser(testUser);
    }
}