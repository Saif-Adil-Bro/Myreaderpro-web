import { createBrowserClient } from '@supabase/ssr'

/**
 * Creates a browser-safe Supabase client for Next.js Client Components.
 * This client runs directly on the browser and automatically sends cookies
 * for user authentication headers.
 */
export function createClient() {
  return createBrowserClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
  )
}
export default createClient;
