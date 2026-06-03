'use client'

import React, { useState } from 'react'
import Link from 'next/link'
import { uploadBookAction } from './actions'

interface PageProps {
  searchParams: {
    error?: string;
    success?: string;
  }
}

export default function UploadPage({ searchParams }: PageProps) {
  const [selectedType, setSelectedType] = useState<'pdf' | 'epub' | 'article'>('pdf')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const error = searchParams?.error
  const success = searchParams?.success

  const categories = [
    'তাফসীর',
    'হাদীস',
    'আকীদা',
    'পারিবারিক বিধান',
    'ফেকাহ',
    'ইসলামী ইতিহাস',
    'অন্যান্য'
  ]

  const handleSubmit = () => {
    setIsSubmitting(true)
  }

  return (
    <div className="flex flex-col min-h-screen bg-[#0A0B0E] text-[#E2E8F0] font-sans" style={{ fontFamily: "'Noto Sans Bengali', 'Inter', system-ui, sans-serif" }}>
      
      {/* Visual background atmospheric lights */}
      <div className="absolute top-[-10%] left-[-15%] w-[600px] h-[600px] rounded-full bg-emerald-500/5 blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-5%] right-[-10%] w-[500px] h-[500px] rounded-full bg-emerald-500/3 blur-[100px] pointer-events-none" />

      {/* Header section */}
      <header className="border-b border-white/5 py-6 px-8 bg-[#0F1115]/80 backdrop-blur sticky top-0 z-40">
        <div className="max-w-4xl mx-auto flex justify-between items-center">
          <div className="flex items-center gap-3">
            <Link href="/library" className="text-slate-400 hover:text-[#10B981] transition-colors p-2 bg-[#1A1D23] rounded-xl border border-white/5">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
            </Link>
            <div>
              <span className="text-[9px] uppercase tracking-widest text-[#10B981] font-bold block mb-0.5">
                সিস্টেম এডমিন প্যানেল
              </span>
              <h1 className="text-xl font-black text-white">
                নতুন কিতাব <span className="text-[#10B981]">সংযোজন</span>
              </h1>
            </div>
          </div>
          
          <span className="text-[11px] text-slate-500 font-bold bg-[#14171C] border border-white/5 px-4 py-2 rounded-xl">
            MyReaderPro v1.0.4
          </span>
        </div>
      </header>

      {/* Upload Form Area using Tabler structures */}
      <main className="max-w-4xl mx-auto px-6 py-12 w-full flex-1">
        <div className="w-full">

          {/* Dynamic state Feedback banner notifications */}
          {error && (
            <div className="p-4 mb-8 bg-red-950/20 border border-red-500/20 rounded-2xl text-red-400 text-sm flex items-start gap-3">
              <svg className="w-5 h-5 flex-shrink-0 mt-0.5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>{decodeURIComponent(error)}</span>
            </div>
          )}

          {success && (
            <div className="p-4 mb-8 bg-emerald-950/20 border border-[#10B981]/20 rounded-2xl text-emerald-400 text-sm flex items-start gap-3">
              <svg className="w-5 h-5 flex-shrink-0 mt-0.5 text-[#10B981]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>{decodeURIComponent(success)}</span>
            </div>
          )}

          <div className="bg-gradient-to-br from-[#1A1D23] to-[#111418] p-8 md:p-10 rounded-[2rem] border border-white/5 shadow-2xl">
            <form action={uploadBookAction} onSubmit={handleSubmit} className="space-y-8">
              
              {/* Informational Sub-heading */}
              <div className="border-b border-white/5 pb-5">
                <h2 className="text-sm font-bold text-white flex items-center gap-2">
                  <span className="w-2 h-2 bg-[#10B981] rounded-full shadow-[0_0_8px_rgba(16,185,129,0.5)]" />
                  কিতাব বা প্রবন্ধের বিবরণাদি
                </h2>
                <p className="text-xs text-slate-400 mt-1">
                  এখানে বইয়ের যাবতীয় মেটাডাটা ও ফাইল ইনপুট দিয়ে লাইব্রেরি ডাটাবেসে হালনাগাদ করুন।
                </p>
              </div>

              {/* Grid 1: Name and Author */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="text-xs uppercase tracking-widest text-slate-400 font-bold block mb-2">
                    কিতাব বা নিবন্ধের নাম <span className="text-[#10B981]">*</span>
                  </label>
                  <input 
                    type="text" 
                    name="title" 
                    placeholder="যেমন: সূরা বাকারাহ তাফসীর" 
                    required 
                    className="w-full bg-[#14171C] border border-white/5 text-white placeholder-slate-600 text-sm rounded-2xl px-5 py-3.5 focus:outline-none focus:border-[#10B981]/50 focus:ring-1 focus:ring-[#10B981]/50 transition-all font-medium"
                  />
                </div>

                <div>
                  <label className="text-xs uppercase tracking-widest text-slate-400 font-bold block mb-2">
                    লেখক বা অনুবাদক <span className="text-[#10B981]">*</span>
                  </label>
                  <input 
                    type="text" 
                    name="author" 
                    placeholder="যেমন: হাফেয ইবনে কাসীর" 
                    required 
                    className="w-full bg-[#14171C] border border-white/5 text-white placeholder-slate-600 text-sm rounded-2xl px-5 py-3.5 focus:outline-none focus:border-[#10B981]/50 focus:ring-1 focus:ring-[#10B981]/50 transition-all font-medium"
                  />
                </div>
              </div>

              {/* Grid 2: Type selection & Category dropdown */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                
                <div>
                  <label className="text-xs uppercase tracking-widest text-slate-400 font-bold block mb-2.5">
                    ফাইলের ধরণ (Format) <span className="text-[#10B981]">*</span>
                  </label>
                  <div className="flex bg-[#14171C] p-1.5 rounded-2xl border border-white/5">
                    <button
                      type="button"
                      onClick={() => setSelectedType('pdf')}
                      className={`flex-1 rounded-xl py-2 text-xs font-bold transition-all ${selectedType === 'pdf' ? 'bg-[#10B981] text-black shadow' : 'text-slate-400 hover:text-white'}`}
                    >
                      PDF
                    </button>
                    <button
                      type="button"
                      onClick={() => setSelectedType('epub')}
                      className={`flex-1 rounded-xl py-2 text-xs font-bold transition-all ${selectedType === 'epub' ? 'bg-[#10B981] text-black shadow' : 'text-slate-400 hover:text-white'}`}
                    >
                      EPUB
                    </button>
                    <button
                      type="button"
                      onClick={() => setSelectedType('article')}
                      className={`flex-1 rounded-xl py-2 text-xs font-bold transition-all ${selectedType === 'article' ? 'bg-[#10B981] text-black shadow' : 'text-slate-400 hover:text-white'}`}
                    >
                      নিবন্ধ (Article)
                    </button>
                  </div>
                  {/* Keep the selected format typed into a hidden form value */}
                  <input type="hidden" name="type" value={selectedType} />
                </div>

                <div>
                  <label className="text-xs uppercase tracking-widest text-slate-400 font-bold block mb-2">
                    ক্যাটাগরি নির্ধারণ করুন <span className="text-[#10B981]">*</span>
                  </label>
                  <select 
                    name="category"
                    className="w-full bg-[#14171C] border border-white/5 text-white text-sm rounded-2xl px-5 py-3.5 focus:outline-none focus:border-[#10B981]/50 focus:ring-1 focus:ring-[#10B981]/50 transition-all cursor-pointer font-medium"
                  >
                    {categories.map((cat) => (
                      <option key={cat} value={cat} className="bg-[#1A1D23] text-white">
                        {cat}
                      </option>
                    ))}
                  </select>
                </div>

              </div>

              {/* Grid 3: File Selection - Cover and Payload Files */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 border-t border-white/5 pt-6">
                
                {/* 1. Cover artwork upload input */}
                <div>
                  <label className="text-xs uppercase tracking-widest text-slate-400 font-bold block mb-2">
                    কভার আর্টওয়ার্ক ছবি (Cover Image)
                  </label>
                  <div className="relative border border-dashed border-white/10 hover:border-[#10B981]/40 rounded-2xl bg-[#14171C] p-6 transition-colors group">
                    <input 
                      type="file" 
                      name="cover"
                      accept="image/*"
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10" 
                    />
                    <div className="text-center flex flex-col items-center">
                      <div className="w-10 h-10 rounded-xl bg-slate-900 flex items-center justify-center mb-3 group-hover:scale-105 transition-transform text-slate-400 group-hover:text-[#10B981]">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                      </div>
                      <span className="text-xs font-bold text-white block mb-0.5">কভার ইমেজ আপলোড করুন</span>
                      <span className="text-[10px] text-slate-500">PNG, JPG বা WEBP (অনধিক ২ মেগাবাইট)</span>
                    </div>
                  </div>
                </div>

                {/* 2. Document file uploads based on format */}
                <div>
                  <label className="text-xs uppercase tracking-widest text-slate-400 font-bold block mb-2">
                    কিতাব বা ডকুমেন্ট ফাইল <span className="text-[#10B981]">*</span>
                  </label>
                  <div className="relative border border-dashed border-white/10 hover:border-[#10B981]/40 rounded-2xl bg-[#14171C] p-6 transition-colors group">
                    <input 
                      type="file" 
                      name="file"
                      accept={selectedType === 'pdf' ? '.pdf' : selectedType === 'epub' ? '.epub' : '.pdf,.epub,.txt'}
                      required={selectedType !== 'article'}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10" 
                    />
                    <div className="text-center flex flex-col items-center">
                      <div className="w-10 h-10 rounded-xl bg-slate-900 flex items-center justify-center mb-3 group-hover:scale-105 transition-transform text-slate-400 group-hover:text-[#10B981]">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                        </svg>
                      </div>
                      <span className="text-xs font-bold text-white block mb-0.5">
                        {selectedType === 'pdf' ? 'PDF ফাইল সিলেক্ট করুন' : selectedType === 'epub' ? 'EPUB ফাইল সিলেক্ট করুন' : 'নিবন্ধ ডকুমেন্ট ফাইল'}
                      </span>
                      <span className="text-[10px] text-slate-500">
                        {selectedType === 'pdf' ? 'সর্বোচ্চ ১০০ মেগাবাইট' : selectedType === 'epub' ? 'EPUB ফাইল টাইপ' : 'PDF বা টেক্সট ফাইল'}
                      </span>
                    </div>
                  </div>
                </div>

              </div>

              {/* Form trigger submission and buttons */}
              <div className="border-t border-white/5 pt-6 flex flex-col sm:flex-row items-center justify-end gap-3.5">
                
                <Link 
                  href="/library" 
                  className="w-full sm:w-auto text-center px-6 py-3.5 rounded-xl border border-white/5 text-slate-400 text-xs font-bold hover:text-white hover:bg-slate-900 transition-colors cursor-pointer"
                >
                  বাতিল করুন
                </Link>

                <button 
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full sm:w-auto flex items-center justify-center gap-2 bg-[#10B981] hover:bg-[#059669] text-black font-extrabold text-xs rounded-xl px-8 py-3.5 shadow-[0_0_20px_rgba(16,185,129,0.3)] transition-all cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isSubmitting ? (
                    <>
                      <div className="w-4 h-4 rounded-full border-t border-r border-black animate-spin" />
                      আপলোড হচ্ছে...
                    </>
                  ) : (
                    <>
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                      </svg>
                      কিতাব সংরক্ষণ করুন
                    </>
                  )}
                </button>

              </div>

            </form>
          </div>

        </div>
      </main>

      {/* Admin footer */}
      <footer className="border-t border-white/5 py-8 text-center text-xs text-slate-500 bg-[#07080B] z-10 mt-auto">
        MyReaderPro Admin Interface • Digital Islamic Library Dashboard
      </footer>

    </div>
  )
}
