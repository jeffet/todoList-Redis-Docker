USE `todo`;

delimiter $$

CREATE TABLE `todo` (
  `id` int(11) NOT NULL auto_increment,
  `todo` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1$$