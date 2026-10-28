INSERT INTO users (external_key, display_name)
VALUES ('demo-user', '演示用户')
ON DUPLICATE KEY UPDATE display_name = VALUES(display_name);

INSERT INTO templates (
    id, title, category, cover_url, description,
    required_image_count, accepted_mime_types, max_file_size_bytes, enabled
)
VALUES
    ('demo-postcard', '旅行明信片', '创意图片', '/demo/postcard-cover.svg', '上传图片，模拟生成一张旅行明信片', 2, 'image/jpeg,image/png', 5242880, TRUE),
    ('demo-collage', '照片拼贴', '照片设计', '/demo/collage-cover.svg', '上传三张图片，模拟生成一张照片拼贴', 3, 'image/jpeg,image/png', 8388608, TRUE),
    ('demo-avatar', '社交头像', '个人形象', '/demo/avatar-cover.svg', '上传一张图片，模拟生成社交头像', 1, 'image/jpeg,image/png', 5242880, TRUE)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    category = VALUES(category),
    cover_url = VALUES(cover_url),
    description = VALUES(description),
    required_image_count = VALUES(required_image_count),
    accepted_mime_types = VALUES(accepted_mime_types),
    max_file_size_bytes = VALUES(max_file_size_bytes),
    enabled = VALUES(enabled);
