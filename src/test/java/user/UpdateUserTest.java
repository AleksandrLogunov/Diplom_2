package user;

import client.UserClient;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import model.LoginCredentials;
import model.User;
import org.junit.jupiter.api.*;
import static org.hamcrest.Matchers.*;

public class UpdateUserTest {

    UserClient userClient;
    User testUser;
    String token;
    User conflictingUser;

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
        Faker faker = new Faker();
        String newName = faker.name().firstName();
        testUser.setName(newName);
        userClient.updateUser(testUser, token)
                .then().statusCode(200)
                .body("user.name", equalTo(newName));
    }

    @Test
    @DisplayName("Обновление email с авторизацией")
    void updateEmailWithAuth() {
        Faker faker = new Faker();
        String newEmail = faker.internet().emailAddress();
        testUser.setEmail(newEmail);

        userClient.updateUser(testUser, token)
                .then().statusCode(200)
                .body("success", is(true))
                .body("user.email", equalTo(newEmail));
    }

    @Test
    @DisplayName("Попытка обновления email на уже занятый email")
    void updateEmailToAlreadyExistingEmailFails() {
        conflictingUser = User.getRandomUser();
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
        Faker faker = new Faker();
        String newPassword = faker.internet().password(6, 10);
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
        Faker faker = new Faker();
        testUser.setName(faker.name().firstName());
        userClient.updateUser(testUser, null)
                .then().statusCode(401)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @Test
    @DisplayName("Обновление email без авторизации")
    void updateEmailWithoutAuth() {
        Faker faker = new Faker();
        testUser.setEmail(faker.internet().emailAddress());
        userClient.updateUser(testUser, null)
                .then().statusCode(401)
                .body("message", is("You should be authorised"));
    }

    @Test
    @DisplayName("Попытка обновления пароля без авторизации")
    void updatePasswordWithoutAuthFails() {
        Faker faker = new Faker();
        testUser.setPassword(faker.internet().password(6, 10));
        userClient.updateUser(testUser, null)
                .then().statusCode(401)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @AfterEach
    void tearDown() {
        userClient.deleteUser(testUser);
        if (conflictingUser != null) {
            userClient.deleteUser(conflictingUser);
    }
}
}