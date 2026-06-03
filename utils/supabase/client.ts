import { createBrowserClient } from '@supabase/ssr'

/**
 * Creates a browser-safe Supabase client for Next.js Client Components.
 * This client runs directly on the browser and automatically sends cookies
 * for user authentication headers.
 */
export function createClient() {
  const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://placeholder-url.supabase.co'
  const supabaseKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || 'placeholder-anon-key'

  return createBrowserClient(
    supabaseUrl,
    supabaseKey
  )
}
export default createClient;
