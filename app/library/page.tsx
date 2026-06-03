'use client'

import React, { useState, useEffect } from 'react'
import Link from 'next/link'
import { createClient } from '@/utils/supabase/client'

interface Book {
  id: string;
  title: string;
  author: string;
  type: 'pdf' | 'epub' | 'article';
  cover_url?: string;
  file_url?: string;
  category: string;
  created_at: string;
}

export default function LibraryPage() {
  const [books, setBooks] = useState<Book[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [activeTab, setActiveTab] = useState<'all' | 'books' | 'articles'>('all')
  const [selectedCategory, setSelectedCategory] = useState<string>('সব')

  const supabase = createClient()

  // Dynamic content fetching from Supabase
  useEffect(() => {
    async function loadBooks() {
      try {
        setLoading(true)
        const { data, error } = await supabase
          .from('books')
          .select('*')
          .order('created_at', { ascending: false })

        if (error) {
          console.error('Error fetching library books:', error.message)
        } else if (data && data.length > 0) {
          setBooks(data)
        } else {
          // Fallback initial data so the screen looks fully populated on first open
          setBooks([
            {
              id: '1',
              title: 'তাফসীর ইবনে কাসীর - সূরা আল-বাকারাহ',
              author: 'হাফেয ইবনে কাসীর (রহ.)',
              type: 'pdf',
              category: 'তাফসীর',
              created_at: new Date().toISOString()
            },
            {
              id: '2',
              title: 'সহীহ আল-বুখারী - ১ম খণ্ড',
              author: 'ইমাম বুখারী (রহ.)',
              type: 'epub',
              category: 'হাদীস',
              created_at: new Date().toISOString()
            },
            {
              id: '3',
              title: 'আদর্শ পরিবার গঠন ও পারিবারিক শান্তি',
              author: 'শায়খ ড. সালেহ আল-ফাওজান',
              type: 'article',
              category: 'পারিবারিক বিধান',
              created_at: new Date().toISOString()
            },
            {
              id: '4',
              title: 'বিশুদ্ধ আকীদাহ তাহাবীয়াহ',
              author: 'ইমাম আবু জাফর তাহাবী (রহ.)',
              type: 'epub',
              category: 'আকীদা',
              created_at: new Date().toISOString()
            }
          ])
        }
      } catch (e) {
        console.error('Connection issue:', e)
      } finally {
        setLoading(false)
      }
    }

    loadBooks()
  }, [])

  // Categories extraction
  const categories = ['সব', ...Array.from(new Set(books.map(b => b.category)))]

  // Filter & Search Logic
  const filteredBooks = books.filter(book => {
    const matchesSearch = 
      book.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      book.author.toLowerCase().includes(searchQuery.toLowerCase()) ||
      book.category.toLowerCase().includes(searchQuery.toLowerCase())

    const matchesTab = 
      activeTab === 'all' ||
      (activeTab === 'books' && (book.type === 'pdf' || book.type === 'epub')) ||
      (activeTab === 'articles' && book.type === 'article')

    const matchesCategory = 
      selectedCategory === 'সব' || 
      book.category === selectedCategory

    return matchesSearch && matchesTab && matchesCategory
  })

  return (
    <div className="flex flex-col min-h-screen bg-[#0A0B0E] text-[#E2E8F0]" style={{ fontFamily: "'Noto Sans Bengali', 'Inter', system-ui, sans-serif" }}>
      
      {/* Background decoration blur lights */}
      <div className="absolute top-[-5%] right-[-5%] w-[400px] h-[400px] rounded-full bg-emerald-500/5 blur-[100px] pointer-events-none" />
      <div className="absolute top-[30%] left-[-10%] w-[500px] h-[500px] rounded-full bg-emerald-500/3 blur-[120px] pointer-events-none" />

      {/* Main Library Navigation Header */}
      <header className="border-b border-white/5 py-6 px-8 bg-[#0F1115]/80 backdrop-blur sticky top-0 z-40">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          
          <div>
            <span className="text-[10px] uppercase tracking-widest text-[#10B981] font-bold block mb-1">
              ডিজিটাল পাঠাগার
            </span>
            <div className="flex items-center gap-3">
              <Link href="/" className="text-white hover:text-[#10B981] transition-colors">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                </svg>
              </Link>
              <h1 className="text-2xl font-black text-white tracking-tight">
                লাইব্রেরি <span className="text-[#10B981]">কালেকশন</span>
              </h1>
            </div>
          </div>

          <div className="flex items-center gap-3 w-full md:w-auto">
            <Link 
              href="/upload" 
              className="flex items-center gap-2 bg-[#10B981] hover:bg-[#059669] text-black text-xs font-bold rounded-xl px-5 py-3 shadow-[0_0_15px_rgba(16,185,129,0.2)] transition-all cursor-pointer w-full md:w-auto justify-center"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M12 4v16m8-8H4" />
              </svg>
              বই আপলোড করুন
            </Link>
          </div>

        </div>
      </header>

      {/* Control Area: Searching & Filters */}
      <main className="max-w-7xl mx-auto px-6 py-8 w-full flex-1 flex flex-col gap-8">
        
        <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-6">
          
          {/* Tabs for PDF/EPUB vs Articles */}
          <div className="flex bg-[#14171C] p-1.5 rounded-2xl border border-white/5">
            <button
              onClick={() => setActiveTab('all')}
              className={`rounded-xl px-5 py-2.5 text-xs font-bold transition-all cursor-pointer ${activeTab === 'all' ? 'bg-[#10B981] text-black shadow' : 'text-slate-400 hover:text-white'}`}
            >
              সব সংকলন
            </button>
            <button
              onClick={() => setActiveTab('books')}
              className={`rounded-xl px-5 py-2.5 text-xs font-bold transition-all cursor-pointer ${activeTab === 'books' ? 'bg-[#10B981] text-black shadow' : 'text-slate-400 hover:text-white'}`}
            >
              ইসলামী কিতাবসমূহ (PDF/EPUB)
            </button>
            <button
              onClick={() => setActiveTab('articles')}
              className={`rounded-xl px-5 py-2.5 text-xs font-bold transition-all cursor-pointer ${activeTab === 'articles' ? 'bg-[#10B981] text-black shadow' : 'text-slate-400 hover:text-white'}`}
            >
              বাংলা নিবন্ধ ও প্রবন্ধসমূহ
            </button>
          </div>

          {/* Quick Search Inputs */}
          <div className="relative w-full lg:w-80">
            <input 
              type="text" 
              placeholder="শিরোনাম বা লেখকের নাম দিয়ে খুঁজুন..." 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-[#14171C] border border-white/5 text-white placeholder-slate-500 text-xs rounded-2xl pl-11 pr-5 py-3 focus:outline-none focus:border-[#10B981]/50 focus:ring-1 focus:ring-[#10B981]/50 transition-all font-medium"
            />
            <div className="absolute inset-y-0 left-4 flex items-center pointer-events-none opacity-40">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            {searchQuery && (
              <button 
                onClick={() => setSearchQuery('')}
                className="absolute inset-y-0 right-4 flex items-center text-slate-500 hover:text-white"
              >
                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            )}
          </div>

        </div>

        {/* Categories Horizontal Carousel */}
        <div className="flex flex-col gap-2">
          <span className="text-[10px] uppercase font-extrabold text-slate-500 tracking-wider flex items-center gap-1.5">
            <span className="w-1 h-1 bg-[#10B981] rounded-full" /> ক্যাটাগরি ফিল্টার:
          </span>
          <div className="flex flex-wrap gap-2">
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`text-[11px] font-bold px-4 py-2 rounded-xl transition-all cursor-pointer border ${selectedCategory === cat ? 'bg-gradient-to-r from-[#10B981] to-[#059669] text-black border-transparent shadow' : 'bg-[#14171C] text-slate-400 border-white/5 hover:text-white'}`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>

        {/* Card Grid displaying Books with Tabler patterns */}
        {loading ? (
          <div className="flex flex-col items-center justify-center py-24 gap-4">
            <div className="w-8 h-8 rounded-full border-t-2 border-r-2 border-[#10B981] animate-spin" />
            <span className="text-xs text-slate-400">পাঠাগারের কিতাবসমূহ লোড করা হচ্ছে...</span>
          </div>
        ) : filteredBooks.length === 0 ? (
          <div className="text-center py-20 bg-[#14171C]/40 border border-white/5 rounded-3xl p-8 max-w-xl mx-auto">
            <div className="w-16 h-16 rounded-full bg-[#1A1D23] flex items-center justify-center mx-auto mb-5 text-[#10B981]">
              <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
              </svg>
            </div>
            <h3 className="text-lg font-bold text-white mb-2">কোনো আইটেম পাওয়া যায়নি</h3>
            <p className="text-xs text-slate-400 leading-relaxed mb-6">
              অনুসন্ধানের মানদণ্ড পরিবর্তন করে পুনরায় চেষ্টা করুন অথবা নতুন বই আপলোড বোতাম চেপে যুক্ত করুন।
            </p>
            <button 
              onClick={() => { setSearchQuery(''); setSelectedCategory('সব'); setActiveTab('all'); }}
              className="px-5 py-2.5 bg-[#1A1D23] border border-white/10 rounded-xl text-xs font-bold text-white hover:bg-[#2D3139] transition-all cursor-pointer"
            >
              ফিল্টার রিসেট করুন
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {filteredBooks.map((book) => (
              <div 
                key={book.id}
                className="bg-gradient-to-br from-[#1A1D23] to-[#111418] rounded-3xl border border-white/5 p-5 flex flex-col justify-between shadow-lg hover:shadow-2xl transition-all hover:translate-y-[-2px] group"
              >
                <div>
                  
                  {/* Card Cover area */}
                  <div className="w-full h-44 rounded-2xl bg-[#2D3139]/50 border border-white/5 mb-4 relative overflow-hidden flex items-center justify-center p-4">
                    {book.cover_url ? (
                      <img 
                        src={book.cover_url} 
                        alt={book.title} 
                        className="max-h-full max-w-full rounded-lg object-contain shadow-md"
                      />
                    ) : (
                      <div className="text-center">
                        <div className="w-12 h-12 rounded-2xl bg-[#10B981]/10 flex items-center justify-center mx-auto mb-2 text-[#10B981]">
                          <span className="font-serif text-xl font-bold">{book.title.slice(0, 1)}</span>
                        </div>
                        <span className="text-[10px] text-[#10B981] font-bold uppercase tracking-widest">{book.category}</span>
                      </div>
                    )}
                    
                    {/* Format Badge overlay */}
                    <div className="absolute top-3 right-3 bg-black/60 backdrop-blur text-[9px] text-[#E2E8F0] font-bold px-2.5 py-1 rounded-lg uppercase tracking-wider border border-white/10">
                      {book.type}
                    </div>
                  </div>

                  {/* Title & Author Info */}
                  <div className="px-1 mb-6">
                    <span className="text-[10px] text-slate-500 font-bold block uppercase mb-1">
                      {book.category}
                    </span>
                    <h3 className="text-base font-extrabold text-white leading-snug mb-1.5 group-hover:text-[#10B981] transition-colors line-clamp-2">
                      {book.title}
                    </h3>
                    <p className="text-xs text-slate-400 font-medium truncate">
                      {book.author}
                    </p>
                  </div>

                </div>

                {/* Bottom read progress actions with Tabler patterns */}
                <div className="border-t border-white/5 pt-4">
                  <a 
                    href={book.file_url || '#'} 
                    target={book.file_url ? "_blank" : undefined}
                    rel="noopener noreferrer"
                    className="w-full flex items-center justify-center gap-2 bg-[#1A1D23] hover:bg-gradient-to-r hover:from-[#10B981] hover:to-[#059669] text-white hover:text-black font-extrabold text-xs rounded-2xl py-3 border border-white/10 hover:border-transparent cursor-pointer shadow-sm hover:shadow-[0_0_15px_rgba(16,185,129,0.3)] transition-all"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253" />
                    </svg>
                    এখনই পড়ুন (Read)
                  </a>
                </div>

              </div>
            ))}
          </div>
        )}

      </main>

      {/* Footer copyright section */}
      <footer className="border-t border-white/5 py-8 text-center text-xs text-slate-500 bg-[#07080B] z-10">
        MyReaderPro • সর্বস্বত্ব সংরক্ষিত ২০২৬ © আধুনিক ইসলামী ডিজিটাল পাঠাগার
      </footer>

    </div>
  )
}
