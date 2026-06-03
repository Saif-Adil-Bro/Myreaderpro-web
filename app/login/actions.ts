'use server'

import { createClient } from '@/utils/supabase/server'
import { redirect } from 'next/navigation'

export async function login(formData: FormData) {
  const email = formData.get('email') as string
  const password = formData.get('password') as string
  const supabase = createClient()

  if (!email || !password) {
    return redirect('/login?error=ইমেইল এবং পাসওয়ার্ড আবশ্যক')
  }

  const { error } = await supabase.auth.signInWithPassword({
    email,
    password,
  })

  if (error) {
    console.error('Login Error:', error.message)
    // Translate standard errors to serene Bengali responses
    let friendlyMessage = 'লগইন ব্যর্থ হয়েছে। আপনার বিবরণ পুনরায় যাচাই করুন।'
    if (error.message.includes('Invalid login credentials')) {
      friendlyMessage = 'ভুল ইমেইল অথবা পাসওয়ার্ড দেওয়া হয়েছে।'
    }
    return redirect(`/login?error=${encodeURIComponent(friendlyMessage)}`)
  }

  return redirect('/')
}

export async function signup(formData: FormData) {
  const email = formData.get('email') as string
  const password = formData.get('password') as string
  const fullName = formData.get('fullName') as string
  const supabase = createClient()

  if (!email || !password || !fullName) {
    return redirect('/login?error=অনুগ্রহ করে সকল তথ্য প্রদান করুন')
  }

  const { error } = await supabase.auth.signUp({
    email,
    password,
    options: {
      data: {
        full_name: fullName,
      },
    },
  })

  if (error) {
    console.error('Signup Error:', error.message)
    return redirect(`/login?error=${encodeURIComponent('নিবন্ধন করতে ব্যর্থ হয়েছে: ' + error.message)}`)
  }

  // Verification standard message
  return redirect('/login?success=আপনার ইমেইলে একটি নিশ্চিতকরণ লিংক পাঠানো হয়েছে। অনুগ্রহ করে ভেরিফাই করুন।')
}
