import { motion } from 'framer-motion'
import { BarChart3, Loader2, RefreshCw, Search } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import {
  ApiError,
  fetchClicks,
  fetchStats,
  type ClickEventView,
  type StatsResponse,
} from '@/api'
import { ClicksChart } from '@/components/ClicksChart'
import { KpiCards } from '@/components/KpiCards'
import { RecentClicksTable } from '@/components/RecentClicksTable'
import { TopList } from '@/components/TopList'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { useToast } from '@/components/Toast'

type Props = {
  code: string
  onCodeChange: (code: string) => void
}

export function AnalyticsSection({ code, onCodeChange }: Props) {
  const { toast } = useToast()
  const [query, setQuery] = useState(code)
  const [stats, setStats] = useState<StatsResponse | null>(null)
  const [clicks, setClicks] = useState<ClickEventView[]>([])
  const [loading, setLoading] = useState(false)
  const [notFound, setNotFound] = useState(false)
  const [loadedCode, setLoadedCode] = useState<string | null>(null)

  const load = useCallback(
    async (raw: string) => {
      const trimmed = raw.trim()
      if (!trimmed) return
      setLoading(true)
      setNotFound(false)
      try {
        const [s, c] = await Promise.all([
          fetchStats(trimmed),
          fetchClicks(trimmed, 100, 0),
        ])
        setStats(s)
        setClicks(c)
        setLoadedCode(trimmed)
      } catch (err) {
        setStats(null)
        setClicks([])
        setLoadedCode(null)
        if (err instanceof ApiError && err.status === 404) {
          setNotFound(true)
        } else if (err instanceof ApiError) {
          toast({ title: 'Stats error', description: err.message, variant: 'error' })
        } else {
          toast({
            title: 'Network error',
            description: 'Could not load analytics.',
            variant: 'error',
          })
        }
      } finally {
        setLoading(false)
      }
    },
    [toast],
  )

  useEffect(() => {
    setQuery(code)
    if (code.trim()) {
      void load(code)
    }
    // Intentionally only re-load when the parent-driven code changes (e.g. "View analytics")
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [code])

  function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    const trimmed = query.trim()
    if (!trimmed) return
    onCodeChange(trimmed)
    if (trimmed === code) {
      void load(trimmed)
    }
  }

  return (
    <section id="analytics" className="mx-auto max-w-6xl px-4 pt-14 pb-20">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: '-40px' }}
        transition={{ duration: 0.45 }}
        className="mb-6"
      >
        <div className="mb-2 inline-flex items-center gap-2 rounded-full bg-[var(--color-muted)] px-3 py-1 text-xs font-medium text-[var(--color-muted-foreground)]">
          <BarChart3 className="h-3.5 w-3.5 text-[var(--color-primary)]" />
          Link analytics
        </div>
        <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">
          Understand every click
        </h2>
        <p className="mt-1 max-w-xl text-sm text-[var(--color-muted-foreground)]">
          Totals, unique visitors, top referrers, and a 14-day trend — powered by
          the same APIs the interview exercise exposes.
        </p>
      </motion.div>

      <Card className="mb-5">
        <CardContent className="pt-5">
          <form onSubmit={onSubmit} className="flex flex-col gap-3 sm:flex-row">
            <div className="relative flex-1">
              <Search className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[var(--color-muted-foreground)]" />
              <Input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Paste a short code (e.g. launch-2026)"
                className="pl-10 font-mono"
                spellCheck={false}
                autoComplete="off"
              />
            </div>
            <div className="flex gap-2">
              <Button type="submit" disabled={loading || !query.trim()}>
                {loading ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" /> Loading
                  </>
                ) : (
                  <>
                    <Search className="h-4 w-4" /> Look up
                  </>
                )}
              </Button>
              {loadedCode ? (
                <Button
                  type="button"
                  variant="outline"
                  disabled={loading}
                  onClick={() => void load(loadedCode)}
                  aria-label="Refresh analytics"
                >
                  <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                </Button>
              ) : null}
            </div>
          </form>
        </CardContent>
      </Card>

      {notFound ? (
        <Card>
          <CardContent className="py-12 text-center">
            <p className="text-lg font-semibold">No link with this code</p>
            <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
              Double-check the short code, or create a new one above.
            </p>
          </CardContent>
        </Card>
      ) : null}

      {!notFound && (loading || stats) ? (
        <div className="space-y-4">
          {stats && !loading ? (
            <p className="text-sm text-[var(--color-muted-foreground)]">
              Showing{' '}
              <span className="font-mono font-medium text-[var(--color-foreground)]">
                /{stats.code}
              </span>
              {' → '}
              <span className="break-all">{stats.originalUrl}</span>
            </p>
          ) : null}

          <KpiCards stats={stats} loading={loading} />

          {stats && !loading && stats.totalClicks === 0 ? (
            <Card>
              <CardContent className="py-10 text-center">
                <p className="text-base font-semibold">Share this URL to see analytics</p>
                <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
                  Zero clicks so far — open the short link once and hit refresh.
                </p>
              </CardContent>
            </Card>
          ) : null}

          <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
            <div className="lg:col-span-2">
              <ClicksChart data={stats?.clicksByDay ?? []} loading={loading} />
            </div>
            <TopList
              title="Top referrers"
              description="Where traffic came from"
              items={stats?.topReferrers ?? []}
              loading={loading}
            />
          </div>

          <TopList
            title="Top user agents"
            description="Browsers & clients"
            items={stats?.topUserAgents ?? []}
            loading={loading}
          />

          <RecentClicksTable rows={clicks} loading={loading} />
        </div>
      ) : null}

      {!loading && !stats && !notFound ? (
        <Card>
          <CardContent className="py-12 text-center">
            <p className="text-base font-semibold">Look up any short code</p>
            <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
              Shorten a link above, then jump here — or paste a code you already have.
            </p>
          </CardContent>
        </Card>
      ) : null}
    </section>
  )
}
