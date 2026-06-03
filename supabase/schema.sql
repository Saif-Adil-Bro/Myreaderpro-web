-- SQL Schema Setup for MyReaderPro (Supabase)
-- Target: Primary PostgreSQL Database on Supabase

-- 1. Profiles Table (Extends Supabase Auth users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    full_name TEXT,
    avatar_url TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS for Profiles
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public read access to profiles" 
    ON public.profiles FOR SELECT 
    USING (true);

CREATE POLICY "Allow individual user to update their own profile" 
    ON public.profiles FOR UPDATE 
    USING (auth.uid() = id);

-- Trigger to automatically create a profile when a new user signs up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, full_name, avatar_url)
    VALUES (
        new.id,
        coalesce(new.raw_user_meta_data->>'full_name', new.raw_user_meta_data->>'name', 'মমিন'),
        new.raw_user_meta_data->>'avatar_url'
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();


-- 2. Books / Articles Table
CREATE TABLE IF NOT EXISTS public.books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    type TEXT CHECK (type IN ('pdf', 'epub', 'article')) NOT NULL,
    cover_url TEXT,
    file_url TEXT,
    category TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS for Books
ALTER TABLE public.books ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow read access to books for any authenticated user" 
    ON public.books FOR SELECT 
    USING (auth.role() = 'authenticated');

-- (Optional) If you want public reading
-- CREATE POLICY "Allow read access to anyone" ON public.books FOR SELECT USING (true);


-- 3. Reading Progress Table
CREATE TABLE IF NOT EXISTS public.reading_progress (
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    book_id UUID REFERENCES public.books(id) ON DELETE CASCADE NOT NULL,
    last_page INT DEFAULT 1, -- used for PDFs & articles
    last_cfi TEXT,           -- used for EPUB location representation
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    PRIMARY KEY (user_id, book_id)
);

-- Enable RLS for Reading Progress
ALTER TABLE public.reading_progress ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own reading progress" 
    ON public.reading_progress FOR SELECT 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own reading progress" 
    ON public.reading_progress FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own reading progress" 
    ON public.reading_progress FOR UPDATE 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own reading progress" 
    ON public.reading_progress FOR DELETE 
    USING (auth.uid() = user_id);
