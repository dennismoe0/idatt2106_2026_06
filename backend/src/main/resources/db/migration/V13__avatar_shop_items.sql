CREATE TABLE avatar_shop_items (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  option_type   VARCHAR(50)  NOT NULL,
  option_value  VARCHAR(100) NOT NULL,
  star_price    INT          NOT NULL,
  display_order INT          NOT NULL DEFAULT 0,
  CONSTRAINT uq_shop_item UNIQUE (option_type, option_value)
);
