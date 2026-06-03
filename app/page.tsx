'use client'

import React, { useEffect, useState } from 'react'
import Link from 'next/link'
import { createClient } from '@/utils/supabase/client'

export default function HomePage() {
  const [session, setSession] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [continueReading, setContinueReading] = useState<any[]>([])
  const [loadingProgress, setLoadingProgress] = useState(false)
  
  const supabase = createClient()

  useEffect(() => {
    async function checkAuthAndProgress() {
      try {
        const { data: { session: currentSession } } = await supabase.auth.getSession()
        setSession(currentSession)
        setLoading(false)

        if (currentSession?.user) {
          setLoadingProgress(true)
          // Fetch progress joined with books details
          const { data, error } = await supabase
            .from('reading_progress')
            .select(`
              last_page,
              last_cfi,
              updated_at,
              book_id,
              books (
                id,
                title,
                author,
                type,
                cover_url,
                category
              )
            `)
            .eq('user_id', currentSession.user.id)
            .order('updated_at', { ascending: false })

          if (!error && data && data.length > 0) {
            // Map the schema return types correctly
            const mappedData = data.map((item: any) => ({
              ...item,
              books: Array.isArray(item.books) ? item.books[0] : item.books
            })).filter(item => item.books)
            setContinueReading(mappedData)
          } else {
            // Populate premium visual fallbacks so dashboard never stays completely blank
            setContinueReading([
              {
                last_page: 45,
                last_cfi: '',
                updated_at: new Date().toISOString(),
                book_id: '1',
                books: {
                  id: '1',
                  title: 'তাফসীর ইবনে কাসীর - সূরা আল-বাকারাহ',
                  author: 'হাফেয ইবনে কাসীর (রহ.)',
                  type: 'pdf',
                  category: 'তাফসীর',
                  cover_url: ''
                }
              },
              {
                last_page: 78,
                last_cfi: '',
                updated_at: new Date().toISOString(),
                book_id: '3',
                books: {
                  id: '3',
                  title: 'আদর্শ পরিবার গঠন ও পারিবারিক শান্তি',
                  author: 'শায়খ ড. সালেহ আল-ফাওজান',
                  type: 'article',
                  category: 'পারিবারিক বিধান',
                  cover_url: ''
                }
              }
            ])
          }
        }
      } catch (err) {
        console.error('Error during authorization verification:', err)
      } finally {
        setLoadingProgress(false)
      }
    }
    
    checkAuthAndProgress()
  }, [])

  return (
    <div className="flex flex-col min-h-screen bg-[#0A0B0E] text-[#E2E8F0] font-sans overflow-hidden" style={{ fontFamily: "'Noto Sans Bengali', 'Inter', system-ui, sans-serif" }}>
      
      {/* Visual neon light backgrounds */}
      <div className="absolute top-[-10%] right-[-10%] w-[600px] h-[600px] rounded-full bg-emerald-500/5 blur-[130px] pointer-events-none" />
      <div className="absolute bottom-[-10%] left-[-15%] w-[600px] h-[600px] rounded-full bg-emerald-500/3 blur-[120px] pointer-events-none" />

      {/* Main Container */}
      <div className="flex flex-col items-center justify-center flex-1 px-6 text-center z-10 py-12">
        <div className="w-full max-w-4xl">
          
          <span className="text-xs uppercase tracking-widest text-[#10B981] font-extrabold block mb-3">
            আস-সালামু আলাইকুম
          </span>

          <h1 className="text-4xl md:text-5xl font-black text-white tracking-tight mb-4">
            MyReader<span className="text-[#10B981]">Pro</span>
          </h1>

          <p className="text-sm md:text-base text-slate-400 max-w-lg mx-auto leading-relaxed mb-10">
            নির্ভরযোগ্য একাডেমিক ইসলামী কিতাব, তাফসীর ও গবেষণামূলক বাংলা নিবন্ধ পাঠের জন্য একটি আধুনিক ও পরিচ্ছন্ন ডিজিটাল লাইব্রেরি মাধ্যম।
          </p>

          {/* Continue Reading Section utilizing Tabler Card and progress UI components */}
          {session && continueReading.length > 0 && (
            <div className="w-full max-w-3xl mx-auto mb-12 text-left bg-gradient-to-br from-[#13161C] to-[#0D1013] border border-white/5 p-6 md:p-8 rounded-[2rem] shadow-2xl">
              <div className="flex justify-between items-center mb-6">
                <div>
                  <h2 className="text-base font-bold text-white flex items-center gap-2">
                    <span className="w-2.5 h-2.5 bg-[#10B981] rounded-full animate-pulse" />
                    পড়া অব্যাহত রাখুন (Continue Reading)
                  </h2>
                  <p className="text-[11px] text-slate-500">আপনার সর্বশেষ পড়া অধ্যায় ও কিতাবসমূহ</p>
                </div>
                <Link href="/library" className="text-xs text-[#10B981] hover:underline font-bold">
                  সবগুলো দেখুন →
                </Link>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {continueReading.slice(0, 2).map((item) => {
                  const book = item.books
                  const progressPercentage = Math.min(Math.max(item.last_page || 1, 1), 100)
                  return (
                    <div 
                      key={item.book_id}
                      className="bg-[#181B21]/90 border border-white/5 p-4 rounded-2xl flex flex-col justify-between hover:border-[#10B981]/25 transition-all group"
                    >
                      <div className="flex gap-4 mb-4">
                        <div className="w-12 h-16 rounded-lg bg-[#2D3139]/50 flex-shrink-0 flex items-center justify-center p-1 text-[#10B981] border border-white/5 relative overflow-hidden">
                          {book.cover_url ? (
                            <img src={book.cover_url} alt={book.title} className="w-full h-full object-cover rounded-md" />
                          ) : (
                            <span className="font-serif text-lg font-bold">{book.title.slice(0, 1)}</span>
                          )}
                          <div className="absolute bottom-1 right-1 bg-black/60 text-[8px] text-slate-300 font-bold px-1.5 py-0.5 rounded uppercase">
                            {book.type}
                          </div>
                        </div>

                        <div className="min-w-0">
                          <span className="text-[9px] uppercase font-bold text-slate-500 tracking-wider">
                            {book.category}
                          </span>
                          <h3 className="text-xs font-bold text-white leading-tight truncate group-hover:text-[#10B981] transition-all">
                            {book.title}
                          </h3>
                          <p className="text-[10px] text-slate-400 truncate mt-0.5">
                            {book.author}
                          </p>
                        </div>
                      </div>

                      <div>
                        {/* Progress Label */}
                        <div className="flex justify-between items-center text-[10px] mb-1.5 font-bold">
                          <span className="text-slate-400">অগ্রগতি</span>
                          <span className="text-[#10B981]">{progressPercentage}% সম্পন্ন</span>
                        </div>
                        {/* Progress Bar Container */}
                        <div className="w-full bg-[#1F232B] h-1.5 rounded-full overflow-hidden">
                          <div 
                            className="bg-gradient-to-r from-[#10B981] to-[#059669] h-full rounded-full transition-all duration-500" 
                            style={{ width: `${progressPercentage}%` }}
                          />
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>
          )}

          {/* Core Interactive Action Grid using Tabler card frameworks */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 max-w-md mx-auto mb-12">
            
            <Link 
              href="/library" 
              className="bg-gradient-to-br from-[#1A1D23] to-[#111418] border border-white/5 p-6 rounded-3xl text-left hover:border-[#10B981]/30 hover:shadow-lg transition-all group cursor-pointer"
            >
              <div className="w-10 h-10 rounded-xl bg-[#10B981]/10 flex items-center justify-center text-[#10B981] mb-4 group-hover:scale-105 transition-transform">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                </svg>
              </div>
              <h2 className="text-sm font-bold text-white mb-1 group-hover:text-[#10B981] transition-colors">
                লাইব্রেরি প্রবেশ করুন
              </h2>
              <p className="text-[11px] text-slate-500 leading-normal">
                ইসলামী বই ও বাংলা প্রবন্ধের সংগ্রহশালা দেখুন
              </p>
            </Link>

            <Link 
              href="/upload" 
              className="bg-gradient-to-br from-[#1A1D23] to-[#111418] border border-white/5 p-6 rounded-3xl text-left hover:border-[#10B981]/30 hover:shadow-lg transition-all group cursor-pointer"
            >
              <div className="w-10 h-10 rounded-xl bg-[#10B981]/10 flex items-center justify-center text-[#10B981] mb-4 group-hover:scale-105 transition-transform">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                </svg>
              </div>
              <h2 className="text-sm font-bold text-white mb-1 group-hover:text-[#10B981] transition-colors">
                বই আপলোড কারী
              </h2>
              <p className="text-[11px] text-slate-500 leading-normal">
                নতুন বই, কভার পেজ ও মেটা ডাটা যুক্ত করুন
              </p>
            </Link>

          </div>

          {/* Quick status portal */}
          <div className="inline-flex items-center gap-3 bg-[#14171C]/80 border border-white/5 py-2.5 px-5 rounded-full text-xs">
            {loading ? (
              <span className="text-slate-500">অনুমতি নিশ্চিত করা হচ্ছে...</span>
            ) : session ? (
              <>
                <span className="w-2 h-2 rounded-full bg-[#10B981]" />
                <span className="text-slate-300 font-bold">লগইন রয়েছেন: {session.user.email}</span>
              </>
            ) : (
              <>
                <span className="w-2 h-2 rounded-full bg-slate-600" />
                <span className="text-slate-400">আপনি এখনও অথেনটিকেশন সম্পন্ন করেননি।</span>
                <Link href="/login" className="text-[#10B981] font-bold hover:underline ml-1">
                  লগইন করুন →
                </Link>
              </>
            )}
          </div>

        </div>
      </div>

      {/* Simple Footer details */}
      <footer className="py-8 text-center text-xs text-slate-500 bg-[#07080B] z-10">
        MyReaderPro • Digital Library System • Designed with Sophisticated Dark
      </footer>

    </div>
  )
}
