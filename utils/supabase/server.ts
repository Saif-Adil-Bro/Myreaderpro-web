import { createServerClient, type CookieOptions } from '@supabase/ssr'
import { cookies } from 'next/headers'

/**
 * Creates an authorized Supabase client for Next.js Server Components,
 * Server Actions, and Route Handlers. Automatically accesses and syncs
 * Auth session cookies securely.
 */
export function createClient() {
  const cookieStore = cookies()

  const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://phkjmdmjmoxpdduoyfxp.supabase.co'
  const supabaseKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || 'sb_publishable_tMNtA0sQs4o3m8iXr2meqg_dIRjCaIN'

  return createServerClient(
    supabaseUrl,
    supabaseKey,
    {
      cookies: {
        getAll() {
          return cookieStore.getAll()
        },
        setAll(cookiesToSet: Array<{ name: string; value: string; options: CookieOptions }>) {
          try {
            cookiesToSet.forEach(({ name, value, options }) =>
              cookieStore.set(name, value, options)
            )
          } catch {
            // Under Next.js rules, cookie manipulation can throw errors
            // if executed from inside a static or cached Server Component rendering pass.
            // These can be safely ignored if you have middleware refreshing user sessions.
          }
        },
      },
    }
  )
}
export default createClient;
