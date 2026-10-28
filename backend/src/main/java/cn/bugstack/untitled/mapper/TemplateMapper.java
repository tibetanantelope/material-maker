package cn.bugstack.untitled.mapper;

import cn.bugstack.untitled.model.Template;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TemplateMapper {
    List<Template> findEnabled(@Param("category") String category);
    Template findEnabledById(@Param("id") String id);
}
