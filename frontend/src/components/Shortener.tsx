import { AnimatePresence, motion } from 'framer-motion'
import { ChevronDown, Link2, Loader2, Wand2 } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { ApiError, shorten, type ShortenResponse } from '@/api'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { useToast } from '@/components/Toast'
import { cn } from '@/lib/utils'

const ALIAS_PATTERN = /^[A-Za-z0-9_-]{3,32}$/

type Props = {
  onSuccess: (result: ShortenResponse) => void
}

export function Shortener({ onSuccess }: Props) {
  const { toast } = useToast()
  const [url, setUrl] = useState('')
  const [alias, setAlias] = useState('')
  const [advanced, setAdvanced] = useState(false)
  const [urlError, setUrlError] = useState<string | null>(null)
  const [aliasError, setAliasError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [waking, setWaking] = useState(false)
  const wakeTimer = useRef<number | null>(null)

  useEffect(() => {
    return () => {
      if (wakeTimer.current) window.clearTimeout(wakeTimer.current)
    }
  }, [])

  function validateLocal(): boolean {
    let ok = true
    setUrlError(null)
    setAliasError(null)

    const trimmed = url.trim()
    if (!trimmed) {
      setUrlError('Paste a URL to shorten')
      ok = false
    } else {
      try {
        const u = new URL(trimmed)
        if (u.protocol !== 'http:' && u.protocol !== 'https:') {
          setUrlError('Only http and https URLs are allowed')
          ok = false
        }
      } catch {
        setUrlError('That doesn’t look like a valid absolute URL')
        ok = false
      }
    }

    if (alias.trim()) {
      if (!ALIAS_PATTERN.test(alias.trim())) {
        setAliasError('Alias must match [A-Za-z0-9_-]{3,32}')
        ok = false
      }
    }
    return ok
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!validateLocal() || loading) return

    setLoading(true)
    setWaking(false)
    wakeTimer.current = window.setTimeout(() => setWaking(true), 3000)

    try {
      const payload = {
        url: url.trim(),
        ...(alias.trim() ? { alias: alias.trim() } : {}),
      }
      const result = await shorten(payload)
      onSuccess(result)
      toast({
        title: 'Link shortened',
        description: result.shortUrl,
        variant: 'success',
      })
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.code === 'invalid_url' || err.code === 'validation_failed' || err.code === 'malformed_request') {
          setUrlError(err.message)
        } else if (err.code === 'alias_reserved' || err.code === 'alias_conflict') {
          setAdvanced(true)
          setAliasError(err.message)
        } else {
          toast({ title: 'Couldn’t shorten', description: err.message, variant: 'error' })
        }
      } else {
        toast({
          title: 'Network error',
          description: 'Check your connection and try again.',
          variant: 'error',
        })
      }
    } finally {
      if (wakeTimer.current) window.clearTimeout(wakeTimer.current)
      setLoading(false)
      setWaking(false)
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: 0.35, duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
      className="relative z-10 mx-auto -mt-2 max-w-3xl px-4"
    >
      <Card className="overflow-hidden">
        <CardContent className="pt-6">
          <form onSubmit={onSubmit} className="space-y-4" noValidate>
            <div className="space-y-2">
              <label htmlFor="url" className="text-sm font-medium">
                Long URL
              </label>
              <div className="relative">
                <Link2 className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[var(--color-muted-foreground)]" />
                <Input
                  id="url"
                  name="url"
                  autoFocus
                  autoComplete="off"
                  spellCheck={false}
                  placeholder="https://example.com/very/long/path?utm=…"
                  value={url}
                  onChange={(e) => {
                    setUrl(e.target.value)
                    if (urlError) setUrlError(null)
                  }}
                  className={cn('pl-10', urlError && 'ring-2 ring-[var(--color-destructive)]')}
                  aria-invalid={!!urlError}
                />
              </div>
              {urlError ? (
                <p className="text-xs font-medium text-[var(--color-destructive)]">{urlError}</p>
              ) : null}
            </div>

            <button
              type="button"
              onClick={() => setAdvanced((v) => !v)}
              className="inline-flex items-center gap-1.5 text-xs font-medium text-[var(--color-muted-foreground)] transition hover:text-[var(--color-foreground)]"
            >
              <ChevronDown
                className={cn('h-3.5 w-3.5 transition', advanced && 'rotate-180')}
              />
              Advanced · custom alias
            </button>

            <AnimatePresence initial={false}>
              {advanced ? (
                <motion.div
                  key="alias"
                  initial={{ height: 0, opacity: 0 }}
                  animate={{ height: 'auto', opacity: 1 }}
                  exit={{ height: 0, opacity: 0 }}
                  className="overflow-hidden"
                >
                  <div className="space-y-2 pb-1">
                    <label htmlFor="alias" className="text-sm font-medium">
                      Custom alias <span className="font-normal text-[var(--color-muted-foreground)]">(optional)</span>
                    </label>
                    <div className="relative">
                      <Wand2 className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[var(--color-muted-foreground)]" />
                      <Input
                        id="alias"
                        name="alias"
                        autoComplete="off"
                        spellCheck={false}
                        placeholder="launch-2026"
                        value={alias}
                        onChange={(e) => {
                          setAlias(e.target.value)
                          if (aliasError) setAliasError(null)
                        }}
                        className={cn('pl-10 font-mono', aliasError && 'ring-2 ring-[var(--color-destructive)]')}
                        aria-invalid={!!aliasError}
                      />
                    </div>
                    <p className="text-xs text-[var(--color-muted-foreground)]">
                      Pattern: <code className="font-mono">[A-Za-z0-9_-]{'{3,32}'}</code>
                    </p>
                    {aliasError ? (
                      <p className="text-xs font-medium text-[var(--color-destructive)]">{aliasError}</p>
                    ) : null}
                  </div>
                </motion.div>
              ) : null}
            </AnimatePresence>

            <Button type="submit" size="lg" className="w-full sm:w-auto" disabled={loading}>
              {loading ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  {waking ? 'Waking up the server…' : 'Shortening…'}
                </>
              ) : (
                <>
                  <Wand2 className="h-4 w-4" />
                  Shorten link
                </>
              )}
            </Button>
          </form>
        </CardContent>
      </Card>
    </motion.div>
  )
}
