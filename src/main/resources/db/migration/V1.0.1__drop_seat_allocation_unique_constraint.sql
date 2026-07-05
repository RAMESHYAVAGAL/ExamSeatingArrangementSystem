-- Drop the unique constraint from seat_allocation table
-- This constraint name comes from your error: UKo8qkxw9f12l8vkfwvfgktupq9
ALTER TABLE seat_allocation DROP INDEX UKo8qkxw9f12l8vkfwvfgktupq9;

-- If there are any other unique constraints, drop them too
-- ALTER TABLE seat_allocation DROP INDEX another_constraint_name;