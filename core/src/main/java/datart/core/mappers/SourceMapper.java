package datart.core.mappers;

import datart.core.entity.Source;
import datart.core.mappers.ext.CRUDMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;

public interface SourceMapper extends CRUDMapper {
    @Delete({
        "delete from source",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int deleteByPrimaryKey(String id);

    @Insert({
        "insert into source (id, `name`, ",
        "config, `type`, org_id, ",
        "create_by, create_time, ",
        "update_by, update_time, ",
        "`status`, is_folder, ",
        "category, parent_id, ",
        "`index`)",
        "values (#{id,jdbcType=VARCHAR}, #{name,jdbcType=VARCHAR}, ",
        "#{config,jdbcType=VARCHAR}, #{type,jdbcType=VARCHAR}, #{orgId,jdbcType=VARCHAR}, ",
        "#{createBy,jdbcType=VARCHAR}, #{createTime,jdbcType=TIMESTAMP}, ",
        "#{updateBy,jdbcType=VARCHAR}, #{updateTime,jdbcType=TIMESTAMP}, ",
        "#{status,jdbcType=TINYINT}, #{isFolder,jdbcType=TINYINT}, ",
        "#{category,jdbcType=VARCHAR}, #{parentId,jdbcType=VARCHAR}, ",
        "#{index,jdbcType=DOUBLE})"
    })
    int insert(Source record);

    @InsertProvider(type=SourceSqlProvider.class, method="insertSelective")
    int insertSelective(Source record);

    @Select({
        "select",
        "id, `name`, config, `type`, org_id, create_by, create_time, update_by, update_time, ",
        "`status`, is_folder, category, parent_id, `index`",
        "from source",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
        @Result(column="config", property="config", jdbcType=JdbcType.VARCHAR),
        @Result(column="type", property="type", jdbcType=JdbcType.VARCHAR),
        @Result(column="org_id", property="orgId", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="status", property="status", jdbcType=JdbcType.TINYINT),
        @Result(column="is_folder", property="isFolder", jdbcType=JdbcType.TINYINT),
        @Result(column="category", property="category", jdbcType=JdbcType.VARCHAR),
        @Result(column="parent_id", property="parentId", jdbcType=JdbcType.VARCHAR),
        @Result(column="index", property="index", jdbcType=JdbcType.DOUBLE)
    })
    Source selectByPrimaryKey(String id);

    @UpdateProvider(type=SourceSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(Source record);

    @Update({
        "update source",
        "set `name` = #{name,jdbcType=VARCHAR},",
          "config = #{config,jdbcType=VARCHAR},",
          "`type` = #{type,jdbcType=VARCHAR},",
          "org_id = #{orgId,jdbcType=VARCHAR},",
          "create_by = #{createBy,jdbcType=VARCHAR},",
          "create_time = #{createTime,jdbcType=TIMESTAMP},",
          "update_by = #{updateBy,jdbcType=VARCHAR},",
          "update_time = #{updateTime,jdbcType=TIMESTAMP},",
          "`status` = #{status,jdbcType=TINYINT},",
          "is_folder = #{isFolder,jdbcType=TINYINT},",
          "category = #{category,jdbcType=VARCHAR},",
          "parent_id = #{parentId,jdbcType=VARCHAR},",
          "`index` = #{index,jdbcType=DOUBLE}",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int updateByPrimaryKey(Source record);
}