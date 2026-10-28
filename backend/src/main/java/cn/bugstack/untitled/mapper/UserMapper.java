package cn.bugstack.untitled.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT id FROM users WHERE external_key = #{externalKey}")
    Long findIdByExternalKey(@Param("externalKey") String externalKey);
}
