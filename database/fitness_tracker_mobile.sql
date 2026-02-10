-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 15, 2026 at 05:38 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `fitness_tracker_mobile`
--

-- --------------------------------------------------------

--
-- Table structure for table `activity_logs`
--

CREATE TABLE `activity_logs` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `activity_type_id` int(11) NOT NULL,
  `log_date` date NOT NULL DEFAULT curdate(),
  `start_time` time DEFAULT NULL,
  `end_time` time DEFAULT NULL,
  `duration_min` int(11) NOT NULL,
  `calories_burned` decimal(10,2) NOT NULL DEFAULT 0.00,
  `details_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`details_json`)),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `activity_logs`
--

INSERT INTO `activity_logs` (`id`, `user_id`, `activity_type_id`, `log_date`, `start_time`, `end_time`, `duration_min`, `calories_burned`, `details_json`, `created_at`) VALUES
(1, 1, 1, '2026-01-06', '02:41:00', '03:30:00', 49, 601.07, '[{\"key\":\"distance_km\",\"value\":4,\"unit\":\"km\"},{\"key\":\"avg_speed_kmh\",\"value\":4,\"unit\":\"km\\/h\"},{\"key\":\"avg_bpm\",\"value\":40,\"unit\":\"bpm\"}]', '2026-01-05 20:12:00'),
(2, 2, 1, '2026-01-06', '02:58:00', '03:58:00', 60, 584.00, '[{\"key\":\"distance_km\",\"value\":10,\"unit\":\"km\"},{\"key\":\"avg_speed_kmh\",\"value\":10,\"unit\":\"km\\/h\"},{\"key\":\"avg_bpm\",\"value\":200,\"unit\":\"bpm\"}]', '2026-01-05 20:29:07'),
(3, 3, 1, '2026-01-06', '03:57:00', '04:30:00', 33, 330.00, '[{\"key\":\"distance_km\",\"value\":20,\"unit\":\"km\"},{\"key\":\"avg_speed_kmh\",\"value\":10,\"unit\":\"km\\/h\"},{\"key\":\"avg_bpm\",\"value\":100,\"unit\":\"bpm\"}]', '2026-01-05 21:28:13'),
(4, 4, 3, '2026-01-06', '04:26:00', '04:55:00', 29, 435.00, '[{\"key\":\"jumps\",\"value\":40,\"unit\":\"count\"},{\"key\":\"avg_bpm\",\"value\":40,\"unit\":\"bpm\"}]', '2026-01-05 21:57:10'),
(5, 3, 1, '2026-01-06', '05:40:00', '06:00:00', 20, 200.00, '[{\"key\":\"distance_km\",\"value\":1,\"unit\":\"km\"},{\"key\":\"avg_speed_kmh\",\"value\":2,\"unit\":\"km\\/h\"},{\"key\":\"avg_bpm\",\"value\":40,\"unit\":\"bpm\"}]', '2026-01-05 22:10:51');

-- --------------------------------------------------------

--
-- Table structure for table `activity_types`
--

CREATE TABLE `activity_types` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `is_featured` tinyint(1) NOT NULL DEFAULT 0,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  `created_by_user_id` int(11) DEFAULT NULL,
  `default_met` decimal(5,2) DEFAULT NULL,
  `field_keys_json` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `activity_types`
--

INSERT INTO `activity_types` (`id`, `name`, `is_featured`, `sort_order`, `created_by_user_id`, `default_met`, `field_keys_json`, `created_at`) VALUES
(1, 'Running', 1, 1, NULL, 8.00, '[\"distance_km\",\"avg_speed_kmh\",\"avg_bpm\",\"steps\"]', '2026-01-05 19:44:39'),
(2, 'Walking', 1, 2, NULL, 3.50, '[\"distance_km\",\"steps\",\"avg_bpm\"]', '2026-01-05 19:44:39'),
(3, 'Jump Rope', 1, 3, NULL, 12.00, '[\"jumps\",\"avg_bpm\"]', '2026-01-05 19:44:39'),
(4, 'Cycling', 1, 4, NULL, 6.80, '[\"distance_km\",\"avg_speed_kmh\",\"avg_bpm\",\"resistance\"]', '2026-01-05 19:44:39'),
(5, 'Swimming', 1, 5, NULL, 7.00, '[\"distance_km\",\"laps\",\"avg_bpm\"]', '2026-01-05 19:44:39');

-- --------------------------------------------------------

--
-- Table structure for table `goals`
--

CREATE TABLE `goals` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` int(11) NOT NULL,
  `daily_activity_target` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `daily_calories_target` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `city` varchar(120) DEFAULT NULL,
  `country` varchar(120) DEFAULT NULL,
  `address_line` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `goals`
--

INSERT INTO `goals` (`id`, `user_id`, `daily_activity_target`, `daily_calories_target`, `start_date`, `end_date`, `created_at`, `updated_at`, `latitude`, `longitude`, `city`, `country`, `address_line`) VALUES
(1, 1, 13, 940, '2026-01-06', '2026-01-08', '2026-01-05 20:11:35', '2026-01-05 20:11:35', 16.810810810811, 96.125966508153, 'Yangon', 'Myanmar (Burma)', 'R46G+999, Upper Kyi Myin Daing Rd, Yangon, Myanmar (Burma)'),
(2, 2, 15, 1000, '2026-01-06', '2026-01-07', '2026-01-05 20:28:41', '2026-01-05 20:28:41', 16.810810810811, 96.125966508153, 'Yangon', 'Myanmar (Burma)', 'R46G+999, Upper Kyi Myin Daing Rd, Yangon, Myanmar (Burma)'),
(4, 4, 10, 1600, '2026-01-06', '2026-01-08', '2026-01-05 21:56:45', '2026-01-05 21:56:45', 16.728395, 94.7319133, 'Pathein', 'Myanmar (Burma)', 'Hpa Yar Chaung, Myanmar (Burma)'),
(6, 3, 10, 1000, '2026-01-14', '2026-01-16', '2026-01-14 03:03:45', '2026-01-14 03:03:45', 16.728395, 94.7319133, 'Pathein', 'Myanmar (Burma)', 'Hpa Yar Chaung, Myanmar (Burma)');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(120) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `phone` varchar(30) NOT NULL,
  `gender` enum('male','female') DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `weight_kg` int(11) DEFAULT NULL,
  `height_cm` int(11) DEFAULT NULL,
  `goal` varchar(50) DEFAULT NULL,
  `activity_level` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `profile_image` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `full_name`, `username`, `email`, `password_hash`, `phone`, `gender`, `age`, `weight_kg`, `height_cm`, `goal`, `activity_level`, `created_at`, `profile_image`) VALUES
(1, 'Ye Myat Kyaw', 'Ye76200', 'yemyat76@gmail.com', '$2y$10$m7cjP29HtXiHrYOVL4MfueQFGNH23A9/TNAfr4tY56awfkggLKS9a', '09762007221', 'male', 22, 92, 143, 'Lose Weight', 'Beginner', '2026-01-05 19:47:03', 'uploads/profile/u_1_1767643671_3608.jpg'),
(2, 'Thin Zar Wint Kyaw', 'Thin76200', 'thinzar76@gmail.com', '$2y$10$ZhL3jFOMMan04dI1pqBhPuglZTyit.KpSfE/4fsUWXVENeJmeTn6C', '09762007221', 'male', 29, 73, 166, 'Lose Weight', 'Advance', '2026-01-05 20:14:02', 'uploads/profile/u_2_1767644062_4038.jpg'),
(3, 'Khin Wint Wah', 'Khin76200', 'Khin78@gmail.com', '$2y$10$FVrHWrZ9ulBIeX81JYLtmuKd9gQt8fuJN8v7OfuNZWwZ8dPbqOfuq', '09762007221', 'female', 29, 75, 165, 'Lose Weight', 'Advance', '2026-01-05 21:12:39', 'uploads/profile/u_1767647559_59b289ce.jpg'),
(4, 'May Myint Mo', 'May76200', 'May78@gmail.com', '$2y$10$AfYb6ChyiXytUq7Gw2A.guTgTyMCTZvJ6qjVxctwub1Cm3IbNEp/K', '09762007221', 'female', 28, 75, 165, 'Lose Weight', 'Beginner', '2026-01-05 21:40:51', 'uploads/profile/u_4_1767649426_1965.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `workout_data`
--

CREATE TABLE `workout_data` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `workout_type_id` int(11) NOT NULL,
  `start_time` varchar(5) DEFAULT NULL,
  `end_time` varchar(5) DEFAULT NULL,
  `duration_minutes` int(11) DEFAULT NULL,
  `intensity` int(11) DEFAULT NULL,
  `reps` int(11) DEFAULT NULL,
  `weight_lifted` decimal(10,2) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `workout_data`
--

INSERT INTO `workout_data` (`id`, `user_id`, `workout_type_id`, `start_time`, `end_time`, `duration_minutes`, `intensity`, `reps`, `weight_lifted`, `notes`, `created_at`) VALUES
(1, 1, 4, '02:42', '03:00', 18, 100, 5, 5.00, '', '2026-01-05 20:12:33'),
(2, 2, 3, '02:59', '03:59', 60, 21, 0, 10.00, '', '2026-01-05 20:29:43'),
(3, 3, 8, '03:58', '04:20', 22, 59, 4, 10.00, '', '2026-01-05 21:28:36'),
(4, 4, 5, '04:27', '05:27', 60, 50, 2, 20.00, '', '2026-01-05 21:57:30');

-- --------------------------------------------------------

--
-- Table structure for table `workout_types`
--

CREATE TABLE `workout_types` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `workout_types`
--

INSERT INTO `workout_types` (`id`, `name`) VALUES
(1, 'Cardio'),
(2, 'Strength Training'),
(3, 'Yoga'),
(4, 'Pilates'),
(5, 'HIIT'),
(6, 'Cycling'),
(7, 'Swimming'),
(8, 'CrossFit'),
(9, 'Running'),
(10, 'Walking');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `activity_logs`
--
ALTER TABLE `activity_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_date` (`user_id`,`log_date`),
  ADD KEY `idx_type` (`activity_type_id`);

--
-- Indexes for table `activity_types`
--
ALTER TABLE `activity_types`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_type` (`name`,`created_by_user_id`),
  ADD KEY `fk_activity_types_user` (`created_by_user_id`);

--
-- Indexes for table `goals`
--
ALTER TABLE `goals`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_goals_user_period` (`user_id`,`start_date`,`end_date`),
  ADD UNIQUE KEY `uk_goals_user` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `workout_data`
--
ALTER TABLE `workout_data`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `workout_type_id` (`workout_type_id`);

--
-- Indexes for table `workout_types`
--
ALTER TABLE `workout_types`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `activity_logs`
--
ALTER TABLE `activity_logs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `activity_types`
--
ALTER TABLE `activity_types`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `goals`
--
ALTER TABLE `goals`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `workout_data`
--
ALTER TABLE `workout_data`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `workout_types`
--
ALTER TABLE `workout_types`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activity_types`
--
ALTER TABLE `activity_types`
  ADD CONSTRAINT `fk_activity_types_user` FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `goals`
--
ALTER TABLE `goals`
  ADD CONSTRAINT `fk_goals_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `workout_data`
--
ALTER TABLE `workout_data`
  ADD CONSTRAINT `workout_data_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `workout_data_ibfk_2` FOREIGN KEY (`workout_type_id`) REFERENCES `workout_types` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
