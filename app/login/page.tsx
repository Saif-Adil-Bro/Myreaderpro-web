'use client'

import React, { useState } from 'react'
import { login, signup } from './actions'

interface PageProps {
  searchParams: {
    error?: string;
    success?: string;
  }
}

export default function LoginPage({ searchParams }: PageProps) {
  const [isSignUp, setIsSignUp] = useState(false)
  const error = searchParams?.error
  const success = searchParams?.success

  return (
    <div className="flex flex-col min-h-screen bg-[#0A0B0E] text-[#E2E8F0] font-sans overflow-hidden" style={{ fontFamily: "'Noto Sans Bengali', 'Inter', system-ui, sans-serif" }}>
      
      {/* Visual background atmospheric lights */}
      <div className="absolute top-[-10%] left-[-10%] w-[500px] h-[500px] rounded-full bg-emerald-500/5 blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[500px] h-[500px] rounded-full bg-emerald-500/5 blur-[120px] pointer-events-none" />

      {/* Main Container */}
      <div className="flex flex-col items-center justify-center flex-1 px-6 py-12 z-10">
        <div className="w-full max-w-md">
          
          {/* Logo & Header */}
          <div className="text-center mb-8">
            <span className="text-[10px] uppercase tracking-widest text-[#10B981] font-semibold block mb-2">
              আস-সালামু আলাইকুম
            </span>
            <h1 className="text-3xl font-extrabold tracking-tight text-white mb-2">
              MyReader<span className="text-[#10B981]">Pro</span>
            </h1>
            <p className="text-sm text-slate-400">
              {isSignUp ? 'নতুন একাউন্ট তৈরি করুন' : 'আপনার একাউন্টে প্রবেশ করুন'}
            </p>
          </div>

          {/* Feedback Messages */}
          {error && (
            <div className="p-4 mb-6 bg-red-950/20 border border-red-500/20 rounded-2xl text-red-400 text-sm flex items-start gap-3">
              <svg className="w-5 h-5 flex-shrink-0 mt-0.5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>{decodeURIComponent(error)}</span>
            </div>
          )}

          {success && (
            <div className="p-4 mb-6 bg-emerald-950/20 border border-[#10B981]/20 rounded-2xl text-emerald-400 text-sm flex items-start gap-3">
              <svg className="w-5 h-5 flex-shrink-0 mt-0.5 text-[#10B981]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>{decodeURIComponent(success)}</span>
            </div>
          )}

          {/* Authentication Card utilizing Tabler layout paradigms */}
          <div className="bg-gradient-to-br from-[#1A1D23] to-[#111418] p-8 rounded-[2rem] border border-white/5 shadow-2xl">
            <form action={isSignUp ? signup : login} className="space-y-5">
              
              {/* Optional Name Field for Registration */}
              {isSignUp && (
                <div className="form-group">
                  <label className="text-xs uppercase tracking-widest text-slate-400 font-bold block mb-2">
                    সম্পূর্ণ নাম
                  </label>
                  <div className="relative">
                    <input 
                      type="text" 
                      name="fullName" 
                      placeholder="যেমন: আব্দুর রহমান" 
                      required 
                      className="w-full bg-[#14171C] border border-white/5 text-white placeholder-slate-500 text-sm rounded-2xl px-5 py-3.5 focus:outline-none focus:border-[#10B981]/50 focus:ring-1 focus:ring-[#10B981]/50 transition-all"
                    />
                  </div>
                </div>
              )}

              {/* Email Input */}
              <div className="form-group">
                <label className="text-xs uppercase tracking-widest text-slate-400 font-bold block mb-2">
                  ইমেইল এড্রেস
                </label>
                <div className="relative">
                  <input 
                    type="email" 
                    name="email" 
                    placeholder="email@example.com" 
                    required 
                    className="w-full bg-[#14171C] border border-white/5 text-white placeholder-slate-500 text-sm rounded-2xl px-5 py-3.5 focus:outline-none focus:border-[#10B981]/50 focus:ring-1 focus:ring-[#10B981]/50 transition-all"
                  />
                </div>
              </div>

              {/* Password Input */}
              <div className="form-group">
                <div className="flex justify-between items-center mb-2">
                  <label className="text-xs uppercase tracking-widest text-slate-400 font-bold">
                    পাসওয়ার্ড
                  </label>
                  {!isSignUp && (
                    <a href="#forgot" className="text-xs text-[#10B981] hover:underline">
                      পাসওয়ার্ড ভুলে গেছেন?
                    </a>
                  )}
                </div>
                <div className="relative">
                  <input 
                    type="password" 
                    name="password" 
                    placeholder="••••••••" 
                    required 
                    className="w-full bg-[#14171C] border border-white/5 text-white placeholder-slate-500 text-sm rounded-2xl px-5 py-3.5 focus:outline-none focus:border-[#10B981]/50 focus:ring-1 focus:ring-[#10B981]/50 transition-all"
                  />
                </div>
              </div>

              {/* Submit Button */}
              <button 
                type="submit" 
                className="w-full bg-[#10B981] hover:bg-[#059669] text-black font-bold text-sm rounded-2xl py-3.5 shadow-[0_0_20px_rgba(16,185,129,0.3)] hover:shadow-[0_0_25px_rgba(16,185,129,0.5)] transition-all cursor-pointer"
              >
                {isSignUp ? 'নিবন্ধন সম্পন্ন করুন' : 'লগইন করুন'}
              </button>
            </form>

            {/* Navigation toggle */}
            <div className="mt-6 pt-6 border-t border-white/5 text-center">
              <span className="text-xs text-slate-400">
                {isSignUp ? 'আগে থেকেই একাউন্ট আছে?' : 'নতুন একাউন্ট খুলতে চান?'}
              </span>{' '}
              <button 
                onClick={() => setIsSignUp(!isSignUp)} 
                className="text-xs text-[#10B981] font-bold hover:underline"
              >
                {isSignUp ? 'লগইন করুন' : 'নতুন আইডি খুলুন'}
              </button>
            </div>

          </div>

          {/* Small Footer metadata */}
          <div className="text-center mt-8 text-[11px] text-slate-500">
            MyReaderPro • ডিজিটাল ইসলামী পাঠাগার
          </div>

        </div>
      </div>
    </div>
  )
}
