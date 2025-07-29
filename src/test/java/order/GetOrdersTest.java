package order;

import client.OrderClient;
import client.UserClient;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import model.LoginCredentials;
import model.Order;
import model.User;
import org.junit.jupiter.api.*;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.*;

public class GetOrdersTest {

    UserClient userClient;
    OrderClient orderClient;
    User testUser;
    String accessToken;

    @BeforeEach
    @Step("Настройка перед тестом: создание пользователя, логин, получение ID ингредиентов и создание заказа")
    void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        userClient = new UserClient();
        orderClient = new OrderClient();

        String validBunId = orderClient.getRandomBunId();
        String validNonBunIngredientId = orderClient.getRandomNonBunIngredientId();

        testUser = User.getRandomUser();

        userClient.createUser(testUser).then().statusCode(200);
        LoginCredentials creds = new LoginCredentials(testUser.getEmail(), testUser.getPassword());
        accessToken = userClient.loginUser(creds).then().extract().path("accessToken");

        // Создаем один заказ для этого пользователя
        List<String> ingredients = Arrays.asList(validBunId, validNonBunIngredientId);
        Order order = Order.withValidIngredients(ingredients);
        orderClient.createOrderAuthorized(order, accessToken).then().statusCode(200);
    }

    @AfterEach
    @Step("Удаление тестового пользователя после теста")
    void tearDown() {
        userClient.deleteUser(testUser);
    }

    @Test
    @DisplayName("Получение заказов авторизованным пользователем")
    void getOrdersOfAuthorizedUserSuccessfully() {
        orderClient.getOrdersAuthorized(accessToken)
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("orders", not(empty()));
    }

    @Test
    @DisplayName("Получение заказов неавторизованным пользователем")
    void getOrdersOfUnauthorizedUserFails() {
        orderClient.getOrdersUnauthorized()
                .then()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @Test
    @DisplayName("Получение заказов авторизованным пользователем, у которого нет заказов")
    void getOrdersOfAuthorizedUserWithoutOrders() {
        User newUserWithoutOrders = User.getRandomUser();
        userClient.createUser(newUserWithoutOrders).then().statusCode(200);
        LoginCredentials creds = new LoginCredentials(newUserWithoutOrders.getEmail(), newUserWithoutOrders.getPassword());
        String newToken = userClient.loginUser(creds).then().extract().path("accessToken");

        orderClient.getOrdersAuthorized(newToken)
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("orders", empty());

        userClient.deleteUser(newUserWithoutOrders);
    }
}