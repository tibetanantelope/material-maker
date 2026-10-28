package cn.bugstack.untitled.mapper;

import cn.bugstack.untitled.model.Asset;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface AssetMapper {
    @Insert("""
            INSERT INTO assets (user_id, original_filename, stored_filename, storage_path,
                mime_type, file_size_bytes, upload_status)
            VALUES (#{userId}, #{originalFilename}, #{storedFilename}, #{storagePath},
                #{mimeType}, #{fileSizeBytes}, #{uploadStatus})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Asset asset);

    List<Asset> findOwnedByIds(long userId, List<Long> assetIds);
}
