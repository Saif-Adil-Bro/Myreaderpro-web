import { createBrowserClient } from '@supabase/ssr'

/**
 * Creates a browser-safe Supabase client for Next.js Client Components.
 * This client runs directly on the browser and automatically sends cookies
 * for user authentication headers.
 */
export function createClient() {
  const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://phkjmdmjmoxpdduoyfxp.supabase.co'
  const supabaseKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || 'sb_publishable_tMNtA0sQs4o3m8iXr2meqg_dIRjCaIN'

  return createBrowserClient(
    supabaseUrl,
    supabaseKey
  )
}
export default createClient;
