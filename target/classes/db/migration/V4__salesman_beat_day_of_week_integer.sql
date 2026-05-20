-- Hibernate maps Java int to INTEGER; V2 used SMALLINT.
ALTER TABLE salesman_beat_assignments
    ALTER COLUMN day_of_week TYPE INTEGER USING day_of_week::integer;
