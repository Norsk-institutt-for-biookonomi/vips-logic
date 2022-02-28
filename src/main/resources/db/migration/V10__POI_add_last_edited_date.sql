ALTER TABLE public.point_of_interest
ADD COLUMN last_edited_time TIMESTAMP WITH TIME ZONE DEFAULT now();

-- One time update
UPDATE public.point_of_interest
SET last_edited_time = '2021-01-01';

