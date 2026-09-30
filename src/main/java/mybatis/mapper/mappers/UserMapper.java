package mybatis.mapper.mappers;

import mvc.dto.User;
import mybatis.annotation.*;

import java.util.List;

@Mapper
@CacheNamespace
public interface UserMapper {

    @Select("SELECT * FROM user")
    List<User> findAll();

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") int id);

    @Insert("INSERT INTO user (name, age) VALUES (#{name}, #{age})")
    @Options(useGeneratedKeys = true)
    int insert(@Param("name") String name, @Param("age") int age);

    @Update("UPDATE user SET age = age + #{delta} WHERE id = #{id}")
    int adjustAge(@Param("id") int id, @Param("delta") int delta);

    @Update("UPDATE user SET name = #{name}, age = #{age} WHERE id = #{id}")
    int update(@Param("id") int id, @Param("name") String name, @Param("age") Integer age);

    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(@Param("id") int id);

    @SelectProvider(type = UserSql.class, method = "byName")
    List<User> findByName(@Param("name") String name);

    @Select("""
            <script>
            SELECT * FROM user WHERE id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">
              #{id}
            </foreach>
            </script>
            """)
    List<User> findByIds(@Param("ids") List<Integer> ids);

    List<User> search(@Param("name") String name, @Param("minAge") Integer minAge);

    @Select("SELECT * FROM user ORDER BY ${column}")
    List<User> findAllOrderBy(@Param("column") String column);

    List<UserView> findAllView();

    @Insert("INSERT INTO user (name, age, role) VALUES (#{name}, #{age}, #{role})")
    @Options(useGeneratedKeys = true)
    int insertWithRole(@Param("name") String name, @Param("age") int age, @Param("role") Role role);

    @Select("SELECT * FROM user WHERE id = #{id}")
    UserRoleView findRoleView(@Param("id") int id);
}
