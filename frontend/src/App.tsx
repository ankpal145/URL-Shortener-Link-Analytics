import { useCallback, useState } from 'react'
import type { ShortenResponse } from '@/api'
import { AnalyticsSection } from '@/components/AnalyticsSection'
import { Hero } from '@/components/Hero'
import { ResultCard } from '@/components/ResultCard'
import { Shortener } from '@/components/Shortener'
import { ThemeToggle } from '@/components/ThemeToggle'
import { ToastProvider } from '@/components/Toast'

function Shell() {
  const [result, setResult] = useState<ShortenResponse | null>(null)
  const [analyticsCode, setAnalyticsCode] = useState('')

  const onSuccess = useCallback((r: ShortenResponse) => {
    setResult(r)
  }, [])

  const onViewAnalytics = useCallback((code: string) => {
    setAnalyticsCode(code)
    document.getElementById('analytics')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, [])

  return (
    <div className="relative min-h-screen">
      <header className="sticky top-0 z-40 border-b border-[var(--color-card-border)]/60 bg-[color-mix(in_oklab,var(--color-background)_75%,transparent)] backdrop-blur-xl">
        <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-4">
          <a href="/" className="flex items-center gap-2 font-semibold tracking-tight">
            <span className="inline-flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-accent)] text-[11px] font-bold text-white shadow">
              P
            </span>
            PulseLink
          </a>
          <nav className="flex items-center gap-1">
            <a
              href="#analytics"
              className="rounded-lg px-3 py-2 text-sm text-[var(--color-muted-foreground)] transition hover:bg-[var(--color-muted)] hover:text-[var(--color-foreground)]"
            >
              Analytics
            </a>
            <a
              href="https://github.com/ankpal145/URL-Shortener-Link-Analytics"
              target="_blank"
              rel="noreferrer"
              className="rounded-lg px-3 py-2 text-sm text-[var(--color-muted-foreground)] transition hover:bg-[var(--color-muted)] hover:text-[var(--color-foreground)]"
            >
              GitHub
            </a>
            <ThemeToggle />
          </nav>
        </div>
      </header>

      <main>
        <Hero />
        <Shortener onSuccess={onSuccess} />
        <ResultCard result={result} onViewAnalytics={onViewAnalytics} />
        <AnalyticsSection code={analyticsCode} onCodeChange={setAnalyticsCode} />
      </main>

      <footer className="border-t border-[var(--color-card-border)]/60 py-8 text-center text-xs text-[var(--color-muted-foreground)]">
        <p>
          PulseLink · privacy-conscious short links ·{' '}
          <span className="text-[var(--color-foreground)]">IPs hashed, never stored raw</span>
        </p>
      </footer>
    </div>
  )
}

export default function App() {
  return (
    <ToastProvider>
      <Shell />
    </ToastProvider>
  )
}
