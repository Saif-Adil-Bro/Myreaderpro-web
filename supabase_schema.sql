-- ============================================================================
-- MyReaderPro Production Database Schema (Supabase PostgreSQL)
-- Description: Production database architecture designed for scalability, security,
--              offline synchronization, and complete multi-tenant boundaries.
-- Target: Supabase PostgreSQL (Schema: public)
-- ============================================================================

-- Enable UUID extension if not already enabled
create extension if not exists "uuid-ossp";

-- Drop existing tables to avoid conflict and support re-execution
drop trigger if exists on_auth_user_created on auth.users cascade;
drop function if exists public.handle_new_user() cascade;
drop table if exists public.ad_blocks cascade;
drop table if exists public.notifications cascade;
drop table if exists public.copyright_claims cascade;
drop table if exists public.book_requests cascade;
drop table if exists public.reading_history cascade;
drop table if exists public.reading_notes cascade;
drop table if exists public.bookmarks cascade;
drop table if exists public.downloads cascade;
drop table if exists public.favorites cascade;
drop table if exists public.books cascade;
drop table if exists public.categories cascade;
drop table if exists public.profiles cascade;

-- ============================================================================
-- SECTION 1: TABLES DEFINITIONS
-- ============================================================================

-- 1. Profiles Table (Linked with Supabase auth.users)
create table public.profiles (
    id uuid references auth.users(id) on delete cascade primary key,
    email text unique not null,
    name text not null,
    profile_picture text default 'avatar_default',
    is_guest boolean default false,
    reading_hours float default 0.0,
    books_read integer default 0,
    total_downloads integer default 0,
    reading_streak integer default 0,
    total_pages_read integer default 0,
    membership_type text default 'FREE' check (membership_type in ('FREE', 'PREMIUM', 'VIP')),
    is_supporter boolean default false,
    total_donation_amount float default 0.0,
    role text default 'USER' check (role in ('USER', 'MODERATOR', 'ADMIN')),
    created_at timestamp with time zone default timezone('utc'::text, now()) not null,
    updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 2. Categories Table
create table public.categories (
    id text primary key,
    name text not null,
    icon_name text not null,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 3. Books Table
create table public.books (
    id text primary key,
    title text not null,
    author text not null,
    category_id text references public.categories(id) on delete set null,
    description text not null,
    cover_url text default '',
    file_url text not null, -- Cloudflare R2 file endpoint lookup URL
    pages integer default 0,
    file_size text default '0 KB',
    language text default 'English',
    rating float default 5.0,
    downloads integer default 0,
    is_featured boolean default false,
    is_premium boolean default false,
    vip_only boolean default false,
    ad_free_available boolean default false,
    seo_url text default '',
    meta_title text default '',
    meta_description text default '',
    tags text default '',
    created_at timestamp with time zone default timezone('utc'::text, now()) not null,
    updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 4. Favorites Table (Many-to-many junction)
create table public.favorites (
    id uuid default gen_random_uuid() primary key,
    user_id uuid references public.profiles(id) on delete cascade not null,
    book_id text references public.books(id) on delete cascade not null,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null,
    unique (user_id, book_id)
);

-- 5. Downloads Table (Tracking download states)
create table public.downloads (
    id uuid default gen_random_uuid() primary key,
    user_id uuid references public.profiles(id) on delete cascade not null,
    book_id text references public.books(id) on delete cascade not null,
    status text default 'COMPLETED' check (status in ('DOWNLOADING', 'COMPLETED', 'FAILED')),
    progress float default 1.0,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null,
    unique (user_id, book_id)
);

-- 6. Bookmarks Table
create table public.bookmarks (
    id uuid default gen_random_uuid() primary key,
    user_id uuid references public.profiles(id) on delete cascade not null,
    book_id text references public.books(id) on delete cascade not null,
    page_number integer not null,
    title text not null,
    snippet text default '',
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 7. Reading Notes Table
create table public.reading_notes (
    id uuid default gen_random_uuid() primary key,
    user_id uuid references public.profiles(id) on delete cascade not null,
    book_id text references public.books(id) on delete cascade not null,
    page_number integer not null,
    text text not null,
    highlight_text text default '',
    color_hex text default '#F59E0B',
    created_at timestamp with time zone default timezone('utc'::text, now()) not null,
    updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 8. Reading History Table (Progress analytics and sync tracking)
create table public.reading_history (
    id uuid default gen_random_uuid() primary key,
    user_id uuid references public.profiles(id) on delete cascade not null,
    book_id text references public.books(id) on delete cascade not null,
    page_number integer not null,
    progress_percent float default 0.0,
    last_read_at timestamp with time zone default timezone('utc'::text, now()) not null,
    unique (user_id, book_id)
);

-- 9. Book Requests Table
create table public.book_requests (
    id uuid default gen_random_uuid() primary key,
    user_id uuid references public.profiles(id) on delete cascade,
    title text not null,
    author text not null,
    publisher text default '',
    notes text default '',
    status text default 'PENDING' check (status in ('PENDING', 'COMPLETED', 'REJECTED')),
    request_count integer default 1,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null,
    updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 10. Copyright Claims (DMCA flow)
create table public.copyright_claims (
    id uuid default gen_random_uuid() primary key,
    user_id uuid references public.profiles(id) on delete set null,
    full_name text not null,
    email text not null,
    organization_name text default '',
    content_title text not null,
    content_url text not null,
    description text not null,
    supporting_documents_info text default '',
    status text default 'PENDING' check (status in ('PENDING', 'REVIEWING', 'APPROVED', 'REJECTED')),
    decision_notes text default '',
    temporary_hidden boolean default false,
    claim_date timestamp with time zone default timezone('utc'::text, now()) not null,
    reviewed_at timestamp with time zone,
    reviewer_email text
);

-- 11. Notifications Table
create table public.notifications (
    id uuid default gen_random_uuid() primary key,
    user_id uuid references public.profiles(id) on delete cascade, -- If null, it is an admin broadcast/global alert
    title text not null,
    message text not null,
    is_read boolean default false,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 12. Ad Blocks Table (Ad network integration / campaigns management)
create table public.ad_blocks (
    id text primary key,
    title text not null,
    ad_type text not null check (ad_type in ('BANNER', 'NATIVE', 'INTERSTITIAL', 'REWARDED')),
    is_enabled boolean default true,
    image_url text default 'default_ad_banner',
    clicks integer default 0,
    impressions integer default 0,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null,
    updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);


-- ============================================================================
-- SECTION 2: AUTOMATED PROFILE HANDLER (Supabase Auth Trigger)
-- ============================================================================

-- Automatically insert a profile row when a new user signs up via Firebase / Supabase Auth
create or replace function public.handle_new_user()
returns trigger as $$
begin
  insert into public.profiles (id, email, name, profile_picture, role, membership_type)
  values (
    new.id,
    new.email,
    coalesce(new.raw_user_meta_data->>'full_name', split_part(new.email, '@', 1)),
    coalesce(new.raw_user_meta_data->>'avatar_url', 'avatar_default'),
    'USER',
    'FREE'
  );
  return new;
end;
$$ language plpgsql security definer;

-- Trigger the function on auth.users insert
create or replace trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();


-- ============================================================================
-- SECTION 3: PERFORMANCE INDEXES
-- ============================================================================

-- Composite and single-index configurations optimize read operations for offline sync and listing
create index idx_books_category on public.books(category_id);
create index idx_books_featured on public.books(is_featured) where is_featured = true;
create index idx_favorites_user on public.favorites(user_id);
create index idx_bookmarks_user_book on public.bookmarks(user_id, book_id);
create index idx_notes_user_book on public.reading_notes(user_id, book_id);
create index idx_history_user on public.reading_history(user_id);
create index idx_book_requests_status on public.book_requests(status);
create index idx_copyright_claims_status on public.copyright_claims(status);
create index idx_notifications_user_unread on public.notifications(user_id) where is_read = false;


-- ============================================================================
-- SECTION 4: ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================

-- Enable RLS on all tables
alter table public.profiles enable row level security;
alter table public.categories enable row level security;
alter table public.books enable row level security;
alter table public.favorites enable row level security;
alter table public.downloads enable row level security;
alter table public.bookmarks enable row level security;
alter table public.reading_notes enable row level security;
alter table public.reading_history enable row level security;
alter table public.book_requests enable row level security;
alter table public.copyright_claims enable row level security;
alter table public.notifications enable row level security;
alter table public.ad_blocks enable row level security;

-- 1. Profiles Policies
create policy "Allow public reading of basic profiles" on public.profiles
    for select using (true);

create policy "Allow users to update own profile" on public.profiles
    for update using (auth.uid() = id);

-- 2. Categories Policies
create policy "Allow select categories for everyone signed in" on public.categories
    for select using (auth.role() = 'authenticated' or true);

create policy "Only admin can manage categories" on public.categories
    for all using (
        exists (
            select 1 from public.profiles
            where profiles.id = auth.uid() and profiles.role = 'ADMIN'
        )
    );

-- 3. Books Policies
create policy "Allow authenticated/anyone to view books" on public.books
    for select using (true);

create policy "Only admin or moderator can manage books catalog" on public.books
    for all using (
        exists (
            select 1 from public.profiles
            where profiles.id = auth.uid() and profiles.role in ('ADMIN', 'MODERATOR')
        )
    );

-- 4. Favorites Policies
create policy "Users can see own favorites" on public.favorites
    for select using (auth.uid() = user_id);

create policy "Users can add favorites for themselves" on public.favorites
    for insert with check (auth.uid() = user_id);

create policy "Users can delete own favorites" on public.favorites
    for delete using (auth.uid() = user_id);

-- 5. Downloads Policies
create policy "Users can manage own downloads" on public.downloads
    for all using (auth.uid() = user_id);

-- 6. Bookmarks Policies
create policy "Users can manage own bookmarks" on public.bookmarks
    for all using (auth.uid() = user_id);

-- 7. Reading Notes Policies
create policy "Users can manage own reading notes" on public.reading_notes
    for all using (auth.uid() = user_id);

-- 8. Reading History Policies
create policy "Users can manage own progress" on public.reading_history
    for all using (auth.uid() = user_id);

-- 9. Book Requests Policies
create policy "Users can view own requests" on public.book_requests
    for select using (auth.uid() = user_id or exists (
        select 1 from public.profiles where profiles.id = auth.uid() and profiles.role in ('ADMIN', 'MODERATOR')
    ));

create policy "Users can create requests" on public.book_requests
    for insert with check (auth.uid() = user_id);

create policy "Only admin or moderator can delete/update requests" on public.book_requests
    for update using (exists (
        select 1 from public.profiles where profiles.id = auth.uid() and profiles.role in ('ADMIN', 'MODERATOR')
    ));

-- 10. Copyright Claims Policies
create policy "Claim owners or admin can view copyright claims" on public.copyright_claims
    for select using (auth.uid() = user_id or email = auth.email() or exists (
        select 1 from public.profiles where profiles.id = auth.uid() and profiles.role = 'ADMIN'
    ));

create policy "Anyone can submit copyright claim" on public.copyright_claims
    for insert with check (true);

create policy "Only admin can review copyright claims" on public.copyright_claims
    for update using (exists (
        select 1 from public.profiles where profiles.id = auth.uid() and profiles.role = 'ADMIN'
    ));

-- 11. Notifications Policies
create policy "Users can read targeted or global notifications" on public.notifications
    for select using (user_id is null or auth.uid() = user_id);

create policy "Only admin or moderator can create notifications" on public.notifications
    for insert with check (exists (
        select 1 from public.profiles where profiles.id = auth.uid() and profiles.role in ('ADMIN', 'MODERATOR')
    ));

-- 12. Ad Blocks Policies
create policy "Allow reading ad campaigns config for everyone" on public.ad_blocks
    for select using (true);

create policy "Only admin can manage ad campaigns" on public.ad_blocks
    for all using (exists (
        select 1 from public.profiles where profiles.id = auth.uid() and profiles.role = 'ADMIN'
    ));
