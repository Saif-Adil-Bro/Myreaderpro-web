import React from 'react'
import './globals.css'

export const metadata = {
  title: 'MyReaderPro - ডিজিটাল ইসলামী লাইব্রেরী',
  description: 'Digital library for Islamic books and Bangla articles',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="bn" style={{ height: '100%', margin: 0 }}>
      <head>
        <meta charSet="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <meta name="theme-color" content="#10B981" />
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
        <link rel="manifest" href="/manifest.json" />
        <link rel="apple-touch-icon" href="/icons/icon-192x192.png" />
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+Bengali:wght@300;400;500;600;700;800&family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet" />
        <style>{`
          body {
            margin: 0;
            background-color: #0A0B0E;
            color: #E2E8F0;
            font-family: 'Noto Sans Bengali', 'Inter', system-ui, sans-serif;
          }
        `}</style>
      </head>
      <body className="bg-[#0A0B0E] text-[#E2E8F0] min-h-screen">
        {children}
      </body>
    </html>
  )
}
