-- phpMyAdmin SQL Dump
-- version 3.5.8.1
-- http://www.phpmyadmin.net
--
-- Host: dd29608.kasserver.com
-- Generation Time: May 12, 2015 at 08:27 PM
-- Server version: 5.6.24-nmm1-log
-- PHP Version: 5.5.22-nmm1

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `d0141d48`
--

-- --------------------------------------------------------

--
-- Table structure for table `achievements`
--

CREATE TABLE IF NOT EXISTS `achievements` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` text NOT NULL,
  `Description` text NOT NULL,
  `Image` text,
  PRIMARY KEY (`ID`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=15 ;

-- --------------------------------------------------------

--
-- Table structure for table `achievement_objectives`
--

CREATE TABLE IF NOT EXISTS `achievement_objectives` (
  `AchievementID` int(11) NOT NULL,
  `AttributeID` int(11) NOT NULL,
  `RequiredValue` int(11) NOT NULL,
  PRIMARY KEY (`AchievementID`,`AttributeID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `achievement_player`
--

CREATE TABLE IF NOT EXISTS `achievement_player` (
  `AchievementID` int(11) NOT NULL,
  `PlayerID` int(11) NOT NULL,
  `unlocked` tinyint(4) NOT NULL,
  PRIMARY KEY (`AchievementID`,`PlayerID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `actionlog`
--

CREATE TABLE IF NOT EXISTS `actionlog` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `time` int(11) NOT NULL,
  `message` text NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=59435 ;

-- --------------------------------------------------------

--
-- Table structure for table `attribute`
--

CREATE TABLE IF NOT EXISTS `attribute` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` text NOT NULL,
  `Description` text NOT NULL,
  `Started` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=785 ;

-- --------------------------------------------------------

--
-- Table structure for table `config`
--

CREATE TABLE IF NOT EXISTS `config` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` text NOT NULL,
  `Value` text NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=3 ;

-- --------------------------------------------------------

--
-- Stand-in structure for view `objectives_achievements`
--
CREATE TABLE IF NOT EXISTS `objectives_achievements` (
`ID` int(11)
,`ANAME` text
,`Description` text
,`Name` text
,`RequiredValue` int(11)
);
-- --------------------------------------------------------

--
-- Table structure for table `player`
--

CREATE TABLE IF NOT EXISTS `player` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` text CHARACTER SET latin1 NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=17 ;

-- --------------------------------------------------------

--
-- Table structure for table `stats`
--

CREATE TABLE IF NOT EXISTS `stats` (
  `attributeID` int(11) NOT NULL,
  `playerID` int(11) NOT NULL,
  `state` int(20) NOT NULL,
  PRIMARY KEY (`attributeID`,`playerID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Stand-in structure for view `unmapped_stats`
--
CREATE TABLE IF NOT EXISTS `unmapped_stats` (
`attributeID` int(11)
,`playerID` int(11)
,`state` int(20)
);
-- --------------------------------------------------------

--
-- Stand-in structure for view `view_player_stats`
--
CREATE TABLE IF NOT EXISTS `view_player_stats` (
`playerName` text
,`attributeName` text
,`value` int(20)
);
-- --------------------------------------------------------

--
-- Structure for view `objectives_achievements`
--
DROP TABLE IF EXISTS `objectives_achievements`;

CREATE ALGORITHM=UNDEFINED DEFINER=`d0141d48`@`%` SQL SECURITY DEFINER VIEW `objectives_achievements` AS select `achievements`.`ID` AS `ID`,`achievements`.`Name` AS `ANAME`,`achievements`.`Description` AS `Description`,`attribute`.`Name` AS `Name`,`achievement_objectives`.`RequiredValue` AS `RequiredValue` from ((`achievements` join `achievement_objectives`) join `attribute`) where ((`achievements`.`ID` = `achievement_objectives`.`AchievementID`) and (`attribute`.`ID` = `achievement_objectives`.`AttributeID`));

-- --------------------------------------------------------

--
-- Structure for view `unmapped_stats`
--
DROP TABLE IF EXISTS `unmapped_stats`;

CREATE ALGORITHM=UNDEFINED DEFINER=`d0141d48`@`%` SQL SECURITY DEFINER VIEW `unmapped_stats` AS select `stats`.`attributeID` AS `attributeID`,`stats`.`playerID` AS `playerID`,`stats`.`state` AS `state` from `stats` where (not(`stats`.`attributeID` in (select `attribute`.`ID` from `attribute`)));

-- --------------------------------------------------------

--
-- Structure for view `view_player_stats`
--
DROP TABLE IF EXISTS `view_player_stats`;

CREATE ALGORITHM=UNDEFINED DEFINER=`d0141d48`@`%` SQL SECURITY DEFINER VIEW `view_player_stats` AS select `player`.`Name` AS `playerName`,`attribute`.`Name` AS `attributeName`,`stats`.`state` AS `value` from ((`player` join `attribute`) join `stats`) where ((`player`.`ID` = `stats`.`playerID`) and (`attribute`.`ID` = `stats`.`attributeID`));

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
