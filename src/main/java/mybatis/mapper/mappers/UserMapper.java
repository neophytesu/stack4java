package mybatis.mapper.mappers;

import mvc.dto.User;
import mybatis.annotation.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user")
    List<User> findAll();

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") int id);

    @Select("SELECT * FROM user ORDER BY id DESC LIMIT 1")
    User findLast();

    @Insert("INSERT INTO user (name, age) VALUES (#{name}, #{age})")
    int insert(@Param("name") String name, @Param("age") int age);

    @Update("UPDATE user SET age = age + #{delta} WHERE id = #{id}")
    int adjustAge(@Param("id") int id, @Param("delta") int delta);

    @Update("UPDATE user SET name = #{name}, age = #{age} WHERE id = #{id}")
    int update(@Param("id") int id, @Param("name") String name, @Param("age") Integer age);

    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(@Param("id") int id);

    @SelectProvider(type = UserSql.class, method = "byName")
    List<User> findByName(@Param("name") String name);
}
