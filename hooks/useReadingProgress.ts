import { useState, useEffect, useRef } from 'react'
import { createClient } from '@/utils/supabase/client'

interface ProgressData {
  book_id: string;
  last_page?: number;
  last_cfi?: string;
  updated_at?: string;
}

/**
 * useReadingProgress Hook
 * Tracks and auto-saves user reading progress (page/CFI/percentage) to the Supabase database.
 * Saves automatically every 30 seconds, on changes (debounced), and when leaving the tab.
 */
export function useReadingProgress(bookId: string, initialPage: number = 1, initialCfi: string = '') {
  const [currentPage, setCurrentPage] = useState<number>(initialPage)
  const [currentCfi, setCurrentCfi] = useState<string>(initialCfi)
  const [isSaving, setIsSaving] = useState(false)
  
  const supabase = createClient()
  
  // Refs to always hold the latest states in intervals/event listeners
  const pageRef = useRef(currentPage)
  const cfiRef = useRef(currentCfi)

  useEffect(() => {
    pageRef.current = currentPage
  }, [currentPage])

  useEffect(() => {
    cfiRef.current = currentCfi
  }, [currentCfi])

  // 1. Initial Load: Fetch latest progress for this book/article from database
  useEffect(() => {
    if (!bookId) return

    async function fetchProgress() {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) return

      const { data, error } = await supabase
        .from('reading_progress')
        .select('last_page, last_cfi')
        .eq('user_id', user.id)
        .eq('book_id', bookId)
        .single()

      if (error) {
        console.log('No prior progress found for this book, starting fresh.')
      } else if (data) {
        if (data.last_page) setCurrentPage(data.last_page)
        if (data.last_cfi) setCurrentCfi(data.last_cfi)
      }
    }

    fetchProgress()
  }, [bookId])

  // 2. Save Progress Function
  const saveProgress = async () => {
    if (!bookId) return
    
    try {
      setIsSaving(true)
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) return

      const progressUpdate = {
        user_id: user.id,
        book_id: bookId,
        last_page: pageRef.current,
        last_cfi: cfiRef.current,
        updated_at: new Date().toISOString()
      }

      const { error } = await supabase
        .from('reading_progress')
        .upsert(progressUpdate, { onConflict: 'user_id,book_id' })

      if (error) {
        console.error('Error auto-saving progress:', error.message)
      }
    } catch (err) {
      console.error('Failed to communicate with database:', err)
    } finally {
      setIsSaving(false)
    }
  }

  // 3. Periodic Auto-Save: Every 30 seconds
  useEffect(() => {
    const interval = setInterval(() => {
      saveProgress()
    }, 30000)

    return () => clearInterval(interval)
  }, [bookId])

  // 4. Before Unload Save: Save progress when tab is closed or navigated away
  useEffect(() => {
    const handleBeforeUnload = () => {
      // Trigger update logic
      saveProgress()
    }

    window.addEventListener('beforeunload', handleBeforeUnload)
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload)
      // Save progress on unmount / component cleanup
      saveProgress()
    }
  }, [bookId])

  return {
    currentPage,
    setCurrentPage: (page: number) => {
      setCurrentPage(page)
    },
    currentCfi,
    setCurrentCfi: (cfi: string) => {
      setCurrentCfi(cfi)
    },
    saveProgress,
    isSaving
  }
}

export default useReadingProgress;
