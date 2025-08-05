package user;

import client.UserClient;
import io.restassured.RestAssured;
import model.User;
import org.junit.jupiter.api.*;
import static org.hamcrest.Matchers.*;

public class CreateUserTest {

    UserClient userClient;
    User testUser;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        userClient = new UserClient();
        testUser = User.getRandomUser();
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    void createUniqueUser() {
        userClient.createUser(testUser)
                .then().statusCode(200)
                .body("success", is(true));
    }

    @Test
    @DisplayName("Создание уже существующего пользователя")
    void createDuplicateUser() {
        userClient.createUser(testUser);
        userClient.createUser(testUser)
                .then().statusCode(403)
                .body("message", is("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без email")
    void createUserWithoutEmail() {
        testUser.setEmail(null);
        userClient.createUser(testUser)
                .then().statusCode(403)
                .body("message", containsString("Email, password and name are required fields"));
    }
    @Test
    @DisplayName("Создание пользователя без пароля")
    void createUserWithoutPassword() {
        testUser.setPassword(null);
        userClient.createUser(testUser)
                .then().statusCode(403)
                .body("success", is(false))
                .body("message", containsString("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    void createUserWithoutName() {
        testUser.setName(null);
        userClient.createUser(testUser)
                .then().statusCode(403)
                .body("success", is(false))
                .body("message", containsString("Email, password and name are required fields"));
    }
    @AfterEach
    void tearDown() {
        userClient.deleteUser(testUser);
    }
}
