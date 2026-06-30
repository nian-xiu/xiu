-- Demo seed data. Combined with the app_meta.schema_initialized flag in
-- DataSeeder, this only runs on the first start of a fresh database.

INSERT INTO users (id, username, password_hash, nickname, phone, email, role, status)
VALUES
    (1, 'admin', '$2a$10$CRrSlxKb2p2Jr0lu.LXCPObIQq8/4tL6yzGS3KequRiWFb2hHsnGm', '商城管理员', '13800000000', 'admin@ssmshop.local', 'ADMIN', 'ACTIVE'),
    (2, 'customer', '$2a$10$V35kllF2j65b4LTYFFF/hedlZKB40wWH75p7kCUZ6V.r2Zqn.z74O', '演示用户', '13900000000', 'customer@ssmshop.local', 'CUSTOMER', 'ACTIVE')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname);

INSERT INTO categories (id, name, description, sort_order, enabled)
VALUES
    (1, '数码电器', '手机、电脑、智能硬件与生活电器', 1, TRUE),
    (2, '居家生活', '厨房、收纳、清洁与家居好物', 2, TRUE),
    (3, '运动户外', '训练装备、户外用品与健康器材', 3, TRUE),
    (4, '食品生鲜', '零食、饮品、粮油与时令生鲜', 4, TRUE),
    (5, '图书文创', '书籍、文具、办公与礼品', 5, TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO products (id, category_id, name, subtitle, description, price, original_price, stock, sales, rating, cover_url, status, featured)
VALUES
    (1, 1, 'HUAWEI Pura X Max', '旗舰影像折叠屏，轻薄高性能', '适合移动办公、旅行拍摄和影音娱乐的高端智能手机。', 6999.00, 7599.00, 96, 328, 4.9, '/uploads/products/huawei-pura-x-max.png', 'ON_SALE', TRUE),
    (2, 1, 'ASUS TUF Gaming F15 Pro 游戏笔记本', '高刷电竞屏，稳定性能释放', '面向游戏玩家、设计师和程序开发者的高性能笔记本。', 7999.00, 8699.00, 42, 146, 4.8, '/uploads/products/asus-tuf-gaming-f15-pro-laptop.png', 'ON_SALE', TRUE),
    (3, 1, 'iKF King 2.0 降噪耳机', '主动降噪，长续航通勤款', '适合通勤、运动和在线会议的蓝牙降噪耳机。', 499.00, 699.00, 260, 740, 4.7, '/uploads/products/ikf-king-2-noise-canceling-headphones.png', 'ON_SALE', FALSE),
    (4, 1, '桌面无线充电台灯', '三色温照明，手机随放随充', '适合书桌、床头柜和办公室的整洁桌面照明方案。', 329.00, 429.00, 87, 174, 4.6, '/uploads/products/desktop-wireless-charging-table-lamp.png', 'ON_SALE', FALSE),
    (5, 1, '复古蓝牙音箱', '木纹箱体，低频温润', '适合书房、客厅和床头使用的桌面无线音箱。', 459.00, 599.00, 74, 203, 4.7, '/uploads/products/retro-bluetooth-speaker.png', 'ON_SALE', TRUE),
    (6, 2, '松木多层置物架', '窄边设计，客厅厨房都好放', '承重稳定，适合小户型收纳和展示。', 169.00, 229.00, 88, 501, 4.6, '/uploads/products/pine-multi-layer-storage-rack.png', 'ON_SALE', TRUE),
    (7, 2, '智能恒温电热水壶', '一键保温，食品级不锈钢内胆', '泡茶、冲奶、咖啡手冲都能精准控温。', 239.00, 299.00, 137, 288, 4.7, '/uploads/products/smart-thermostatic-electric-kettle.png', 'ON_SALE', FALSE),
    (8, 2, '布艺单人休闲椅', '高回弹海绵，阅读角首选', '柔和织物面料，适合客厅、卧室和书房。', 699.00, 899.00, 29, 76, 4.6, '/uploads/products/fabric-single-lounge-chair.png', 'ON_SALE', FALSE),
    (9, 2, '法式亚麻餐桌布', '天然亚麻肌理，日常餐桌更清爽', '适合四人餐桌、下午茶和节日布置，耐看且易搭配。', 159.00, 219.00, 110, 188, 4.7, '/uploads/products/french-style-linen-tablecloth.png', 'ON_SALE', TRUE),
    (10, 2, '精致陶瓷咖啡杯', '细腻釉面，家用与礼赠皆宜', '适合家用咖啡角、办公室和轻礼赠场景。', 189.00, 249.00, 96, 246, 4.8, '/uploads/products/high-end-exquisite-ceramic-coffee-cup.png', 'ON_SALE', FALSE),
    (11, 2, '橡木香薰蜡烛', '木质香调，低烟棉芯', '适合夜读、沐浴和客厅氛围营造。', 119.00, 159.00, 160, 421, 4.7, '/uploads/products/oak-scented-candle.png', 'ON_SALE', FALSE),
    (12, 3, 'UP Run 缓震跑鞋 2026 新款', '轻量回弹，适合日常训练', '兼顾支撑和舒适度的城市跑步训练鞋。', 459.00, 599.00, 95, 220, 4.6, '/uploads/products/up-run-cushion-running-shoes-2026.png', 'ON_SALE', TRUE),
    (13, 3, '可调节哑铃套装', '2.5kg-24kg 快速切换', '居家力量训练更省空间，适合多动作组合。', 899.00, 1099.00, 33, 91, 4.8, '/uploads/products/adjustable-dumbbell-set.png', 'ON_SALE', FALSE),
    (14, 3, '轻量瑜伽垫 Pro', '天然橡胶防滑层，6mm 厚度', '适合瑜伽、普拉提和居家拉伸训练。', 199.00, 269.00, 150, 305, 4.6, '/uploads/products/lightweight-yoga-mat-pro.png', 'ON_SALE', FALSE),
    (15, 3, '不锈钢保温运动水瓶', '24 小时保冷，防漏旋盖', '通勤、健身和户外徒步都方便携带。', 129.00, 169.00, 210, 478, 4.5, '/uploads/products/stainless-steel-insulated-sports-water-bottle.png', 'ON_SALE', FALSE),
    (16, 4, '云南挂耳咖啡礼盒', '中深烘焙，坚果与可可香气', '独立包装，办公室和旅行都方便。', 89.00, 128.00, 300, 912, 4.6, '/uploads/products/yunnan-drip-coffee-gift-box.png', 'ON_SALE', TRUE),
    (17, 4, '每日坚果混合装', '低温烘焙，家庭分享装', '杏仁、腰果、榛子与果干科学配比。', 119.00, 159.00, 210, 633, 4.5, '/uploads/products/daily-nuts-mixed-pack.png', 'ON_SALE', FALSE),
    (18, 4, '云南意式拼配咖啡豆', '焦糖、榛果与黑巧风味', '适合意式机、摩卡壶和手冲浓郁方案。', 128.00, 168.00, 180, 532, 4.8, '/uploads/products/italian-blend-yunnan-coffee-beans.png', 'ON_SALE', TRUE),
    (19, 4, '意式拼配咖啡豆', '醇厚均衡，日常咖啡口粮', '适合拿铁、美式和手冲的稳定拼配豆。', 118.00, 158.00, 190, 486, 4.7, '/uploads/products/italian-blended-coffee-beans.png', 'ON_SALE', FALSE),
    (20, 4, 'Basilur 伯爵奶茶', '佛手柑茶香，丝滑奶感', '适合早餐茶、下午茶和办公室常备。', 79.00, 109.00, 240, 391, 4.5, '/uploads/products/basilur-earl-grey-milk-tea.png', 'ON_SALE', FALSE),
    (21, 4, '手工黄油曲奇铁盒', '法式黄油配方，酥松不腻', '独立小包装，适合下午茶和节日分享。', 98.00, 128.00, 190, 566, 4.6, '/uploads/products/handmade-butter-cookies-tin.png', 'ON_SALE', TRUE),
    (22, 4, '皇冠曲奇饼干', '经典礼盒装，奶香酥脆', '家庭聚会、节日礼赠和办公室零食都合适。', 108.00, 139.00, 180, 438, 4.6, '/uploads/products/crown-cookies.png', 'ON_SALE', FALSE),
    (23, 4, '云南精品咖啡', '产地风味清晰，口感干净', '适合手冲和冷萃，带有花果香与坚果尾韵。', 99.00, 139.00, 160, 352, 4.7, '/uploads/products/yunnan-specialty-coffee.png', 'ON_SALE', TRUE),
    (24, 5, '高效学习计划本', '周计划、项目页、复盘页一册搞定', '适合学生、职场人和自由职业者的时间管理本。', 49.00, 69.00, 180, 480, 4.4, '/uploads/products/efficient-study-plan-book.png', 'ON_SALE', FALSE),
    (25, 5, '机械键盘客制化套件', '热插拔轴座，三模连接', '适合办公与游戏的入门客制化键盘套件。', 499.00, 629.00, 72, 156, 4.8, '/uploads/products/mechanical-keyboard-customization-kit.png', 'ON_SALE', TRUE),
    (26, 5, '四季系列明信片', '四季插画主题，适合手帐拼贴', '纸张厚实，适合寄送、装饰和礼物搭配。', 39.00, 59.00, 320, 690, 4.6, '/uploads/products/four-seasons-series-postcards.png', 'ON_SALE', FALSE),
    (27, 5, '胡桃木实木钢笔', '细尖书写，温润木质握感', '商务签字和日常书写兼顾，附替换墨囊。', 299.00, 399.00, 68, 126, 4.7, '/uploads/products/walnut-solid-wood-fountain-pen.png', 'ON_SALE', TRUE),
    (28, 5, '水彩插画明信片套装', '24 张城市与花园主题', '纸张厚实，适合寄送、装饰和手帐拼贴。', 36.00, 49.00, 320, 690, 4.6, '/uploads/products/watercolor-illustration-postcard-set.png', 'ON_SALE', FALSE)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO addresses (id, user_id, receiver, phone, province, city, district, detail, default_address)
VALUES
    (1, 2, '演示用户', '13900000000', '上海市', '上海市', '浦东新区', '张江高科 88 号 8 幢 801', TRUE)
ON DUPLICATE KEY UPDATE receiver = VALUES(receiver);

INSERT INTO activity_campaigns (id, title, description, reward_type, coin_amount, coupon_type, coupon_name, discount_rate, threshold_amount, reduce_amount, start_at, end_at, quota_limit, coupon_expiry_days, claimed_count, status, flash_sale)
VALUES
    (1, '新人礼包·100 金币', '注册即可领取，金币可在结算时抵扣订单金额。', 'COIN', 100, NULL, '新人礼包', NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 'ACTIVE', FALSE),
    (2, '满 199 减 30', '居家好物专享，下单结算时勾选优惠券即可生效。', 'COUPON', NULL, 'AMOUNT_OFF', '满199减30', NULL, 199.00, 30.00, NULL, NULL, 0, 30, 0, 'ACTIVE', FALSE)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO app_meta (meta_key, meta_value) VALUES ('schema_initialized', 'true')
ON DUPLICATE KEY UPDATE meta_value = VALUES(meta_value);
