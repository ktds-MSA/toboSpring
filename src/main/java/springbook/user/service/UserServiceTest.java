package springbook.user.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springbook.user.dao.Level;
import springbook.user.dao.UserDao;
import springbook.user.domain.User;
import springbook.user.service.UserService;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import static org.junit.Assert.fail;
import static springbook.user.service.UserService.MIN_RECOMMEND_FOR_GOLD;
import static springbook.user.service.UserService.MIN_LOGCOUNT_FOR_SILVER;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:testApplicationContext.xml")
public class UserServiceTest {
    @Autowired
    UserService userService;
    UserDao userDao;
    List<User> users;
    @Autowired
    DataSource dataSource;

    @Autowired
    MailSender mailSender;

    @Before
    public void setUp() {
        users = Arrays.asList(
                new User("bumjin", "박범진", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0, "test1@gmail.com"),
                new User("joytouch", "강명성", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "test2@gmail.com"),
                new User("erwins", "신승한", "p3",Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD-1, "test3@gmail.com"),
                new User("madnite1", "이상호", "p4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "test4@gmail.com"),
                new User("green", "오인규", "p5",Level.GOLD, 100,Integer.MAX_VALUE, "test5@gmail.com")
        );
        userDao = userService.getUserDao();
    }

    @Test
    public void bean() {
        assertThat(this.userService, is(notNullValue()));
    }

    @Test
    @DirtiesContext
    public void upgradeLevels() throws TestUserServiceException, SQLException {
        userDao.deleteAll();

        for (User user : users) userDao.add(user);

        MockMailSender mockMailSender = new MockMailSender();
        userService.setMailSender(mockMailSender);

        userService.upgradeLevels();

        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), true);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), true);
        checkLevelUpgraded(users.get(4), false);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size(), is(2));
        assertThat(request.get(0), is(users.get(1).getEmail()));
        assertThat(request.get(1), is(users.get(3).getEmail()));
    }

    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
        }
        else {
            assertThat(userUpdate.getLevel(), is(user.getLevel()));
        }
    }

    private void checkLevel(User user, Level expectLevel) {
        User userUpdate = userDao.get(user.getId());
        assertThat(userUpdate.getLevel(), is(expectLevel));
    }

    @Test
    public void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4);
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
        assertThat(userWithoutLevelRead.getLevel(), is(userWithoutLevel.getLevel()));

    }



    static class TestUserService extends UserService{
        private String id;

        private TestUserService(String id) {
            this.id = id;
        }
        @Override
        protected void upgradeLevel(User user) throws TestUserServiceException {
            if (user.getId().equals(this.id)) throw new TestUserServiceException();
            super.upgradeLevel(user);
        }
    }

    @Test
    public void upgradeAllOrNothing() throws SQLException {
        UserService testUserService = new TestUserService(users.get(3).getId());

        testUserService.setUserDao(this.userDao);
        testUserService.setDataSource(this.dataSource);
        testUserService.setMailSender(mailSender);

        userDao.deleteAll();

        for(User user : users) userDao.add(user);

        try {
            testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }
        catch (TestUserServiceException e){

        }

        checkLevelUpgraded(users.get(1), false);
    }

}
