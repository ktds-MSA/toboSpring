package springbook.user.dao;

import java.sql.*;
import java.util.EmptyStackException;
import java.util.List;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import springbook.user.domain.User;

import javax.sql.DataSource;

public class UserDao {
    private ConnectionMaker connectionMaker;
    private Connection c;
    private User user;
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public UserDao(){

    }
    public UserDao(DataSource dataSource) {

        this.dataSource = dataSource;
    }

    public void add(final User user) throws SQLException {
        this.jdbcTemplate.update("insert into users(id, name, password) values (?,?,?)", user.getId(),user.getName(),user.getPassword());
    }

    public void deleteAll() throws SQLException {
        this.jdbcTemplate.update("delete from users");
    }

    public User get(String id) throws SQLException {
       return this.jdbcTemplate.queryForObject("select * from users where id = ?", new Object[]{id},
           new RowMapper<User>() {
               @Override
               public User mapRow(ResultSet resultSet, int i) throws SQLException {
                   User user = new User();
                   user.setId(resultSet.getString("id"));
                   user.setName(resultSet.getString("name"));
                   user.setPassword(resultSet.getString("password"));

                   return user;
               }
           });
    }

    public List<User> getAll() {
        return this.jdbcTemplate.query("select * from users order by id",
            new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet resultSet, int i) throws SQLException {
                    User user = new User();
                    user.setId(resultSet.getString("id"));
                    user.setName(resultSet.getString("name"));
                    user.setPassword(resultSet.getString("password"));
                    return user;
                }

            });
    }
    public int getCount() throws SQLException{
        return this.jdbcTemplate.queryForInt("select count(*) from users");
    }


    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }

}
