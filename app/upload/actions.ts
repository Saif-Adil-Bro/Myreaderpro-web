'use server'

import { createClient } from '@/utils/supabase/server'
import { redirect } from 'next/navigation'

/**
 * Server Action for handling multi-part uploads to Supabase.
 * Connects to public Storage buckets (covers / books) and updates the metadata on the database.
 */
export async function uploadBookAction(formData: FormData) {
  const title = formData.get('title') as string
  const author = formData.get('author') as string
  const category = formData.get('category') as string
  const type = formData.get('type') as 'pdf' | 'epub' | 'article'
  
  const coverFile = formData.get('cover') as File
  const docFile = formData.get('file') as File

  if (!title || !author || !category || !type) {
    return redirect('/upload?error=সকল প্রয়োজনীয় তথ্য প্রদান করুন।')
  }

  const supabase = createClient()
  let coverUrl = ''
  let fileUrl = ''

  try {
    // 1. Upload Cover Image to Bucket 'covers' if exits
    if (coverFile && coverFile.size > 0) {
      const coverExt = coverFile.name.split('.').pop() || 'jpg'
      const uniqueCoverName = `${Date.now()}-${Math.random().toString(36).substring(3, 9)}.${coverExt}`
      
      const coverBuffer = await coverFile.arrayBuffer()
      
      const { data, error: uploadError } = await supabase.storage
        .from('covers')
        .upload(uniqueCoverName, coverBuffer, {
          contentType: coverFile.type,
          upsert: true
        })

      if (uploadError) {
        console.error('Core cover upload failed:', uploadError.message)
      } else if (data) {
        // Construct the ultimate public URL of the uploaded image
        const { data: publicData } = supabase.storage
          .from('covers')
          .getPublicUrl(uniqueCoverName)
        
        coverUrl = publicData.publicUrl
      }
    }

    // 2. Upload Document file to Bucket 'documents' if exit
    if (docFile && docFile.size > 0) {
      const docExt = docFile.name.split('.').pop() || 'pdf'
      const uniqueDocName = `${Date.now()}-${Math.random().toString(36).substring(3, 9)}.${docExt}`
      
      const docBuffer = await docFile.arrayBuffer()
      
      const { data, error: docError } = await supabase.storage
        .from('documents')
        .upload(uniqueDocName, docBuffer, {
          contentType: docFile.type,
          upsert: true
        })

      if (docError) {
        console.error('Doc payload upload failed:', docError.message)
      } else if (data) {
        const { data: publicData } = supabase.storage
          .from('documents')
          .getPublicUrl(uniqueDocName)
        
        fileUrl = publicData.publicUrl
      }
    }

    // 3. Save Book record meta metadata to DB
    const { error: dbError } = await supabase
      .from('books')
      .insert({
        title,
        author,
        category,
        type,
        cover_url: coverUrl || null,
        file_url: fileUrl || null,
        created_at: new Date().toISOString()
      })

    if (dbError) {
      console.error('Database insertion error:', dbError.message)
      return redirect(`/upload?error=${encodeURIComponent('লাইব্রেরি ক্যাটালগে তথ্য সংরক্ষণ করতে ব্যর্থ হয়েছে: ' + dbError.message)}`)
    }

  } catch (e: any) {
    console.error('Failure inside action pipeline:', e)
    return redirect(`/upload?error=${encodeURIComponent('আপলোড প্রক্রিয়ায় কোনো বড় সমস্যা হয়েছে: ' + e.message)}`)
  }

  // Redirect to parent library view upon success with a smooth transaction
  return redirect('/library?success=নতুন কিতাব সফলভাবে সংযোজিত ও ক্যাটালগ হালনাগাদ করা হয়েছে।')
}
