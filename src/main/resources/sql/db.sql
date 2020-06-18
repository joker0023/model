CREATE TABLE `model_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `url` varchar(256) DEFAULT NULL,
  `title` varchar(128) DEFAULT NULL,
  `cover_img` varchar(256) DEFAULT NULL,
  `local_cover_img` varchar(256) DEFAULT NULL,
  `detail_img` varchar(256) DEFAULT NULL,
  `local_detail_img` varchar(256) DEFAULT NULL,
  `type` varchar(16) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `open` bit(1) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create index idx_title on model_item(`title`);
create index idx_type_open on model_item(`type`, `open`);

CREATE TABLE `item_img` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_id` bigint(20) DEFAULT NULL,
  `detail_img` varchar(256) DEFAULT NULL,
  `local_detail_img` varchar(256) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create index idx_itemid on item_img(`item_id`);
