-- Seed shop items (neon hair colors + new styles)
INSERT INTO avatar_shop_items (option_type, option_value, star_price, display_order) VALUES
  ('hairColor', '#CCFF00',  1, 10),
  ('hairColor', '#39FF14',  2, 11),
  ('hairColor', '#FF4500',  2, 12),
  ('hairColor', '#FF2020',  2, 13),
  ('hairColor', '#CC00FF',  3, 14),
  ('hairStyle', 'wavy',     2, 20),
  ('hairStyle', 'mohawk',   3, 21),
  ('outfit',    'bomber',   3, 30),
  ('outfit',    'trench-coat', 3, 31);

-- Backfill: students who earned medals before this feature shipped get their rewards now
INSERT IGNORE INTO unlocked_avatar_options (student_id, option_type, option_value, source, stop_id, created_at)
SELECT DISTINCT
    sm.student_id,
    CASE s.order_index
        WHEN 1 THEN 'accessory'
        WHEN 2 THEN 'accessory'
        WHEN 3 THEN 'accessory'
        WHEN 4 THEN 'hairColor'
        WHEN 5 THEN 'accessory'
        WHEN 6 THEN 'hairColor'
        WHEN 7 THEN 'outfit'
    END,
    CASE s.order_index
        WHEN 1 THEN 'glasses'
        WHEN 2 THEN 'badge'
        WHEN 3 THEN 'magnifier'
        WHEN 4 THEN '#00E5FF'
        WHEN 5 THEN 'hat'
        WHEN 6 THEN '#FF0099'
        WHEN 7 THEN 'cyber-suit'
    END,
    'medal',
    s.id,
    NOW()
FROM student_medals sm
JOIN medals m ON sm.medal_id = m.id
JOIN stops s ON m.stop_id = s.id
WHERE s.order_index BETWEEN 1 AND 7;
