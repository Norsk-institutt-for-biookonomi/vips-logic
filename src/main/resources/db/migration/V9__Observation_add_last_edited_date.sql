ALTER TABLE public.observation
ADD COLUMN last_edited_time TIMESTAMP WITH TIME ZONE DEFAULT now();

-- One-time update of existing records
UPDATE public.observation
SET last_edited_time = status_changed_time;
