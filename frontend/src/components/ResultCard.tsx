import { AnimatePresence, motion } from 'framer-motion'
import {
  BarChart3,
  Check,
  Copy,
  ExternalLink,
  QrCode,
} from 'lucide-react'
import { useState } from 'react'
import type { ShortenResponse } from '@/api'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { QrDialog } from '@/components/QrDialog'

type Props = {
  result: ShortenResponse | null
  onViewAnalytics: (code: string) => void
}

export function ResultCard({ result, onViewAnalytics }: Props) {
  const [copied, setCopied] = useState(false)
  const [qrOpen, setQrOpen] = useState(false)

  async function copy() {
    if (!result) return
    try {
      await navigator.clipboard.writeText(result.shortUrl)
      setCopied(true)
      window.setTimeout(() => setCopied(false), 1600)
    } catch {
      // ignore — clipboard may be blocked
    }
  }

  return (
    <AnimatePresence mode="wait">
      {result ? (
        <motion.div
          key={result.code}
          initial={{ opacity: 0, y: 16, scale: 0.98 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: 8 }}
          transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
          className="mx-auto mt-5 max-w-3xl px-4"
        >
          <Card className="overflow-hidden">
            <CardContent className="space-y-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <p className="text-xs font-medium tracking-wide text-[var(--color-muted-foreground)] uppercase">
                  Your short link
                  {result.customAlias ? ' · custom alias' : ''}
                </p>
                <span className="rounded-full bg-[color-mix(in_oklab,var(--color-success)_18%,transparent)] px-2.5 py-0.5 text-[11px] font-semibold text-[var(--color-success)]">
                  Ready
                </span>
              </div>

              <a
                href={result.shortUrl}
                target="_blank"
                rel="noreferrer"
                className="block break-all font-mono text-xl font-semibold text-gradient sm:text-2xl"
              >
                {result.shortUrl}
              </a>

              <p className="truncate text-sm text-[var(--color-muted-foreground)]">
                → {result.originalUrl}
              </p>

              <div className="flex flex-wrap gap-2">
                <Button onClick={copy} variant="default" size="sm">
                  {copied ? (
                    <>
                      <Check className="h-4 w-4" /> Copied
                    </>
                  ) : (
                    <>
                      <Copy className="h-4 w-4" /> Copy
                    </>
                  )}
                </Button>
                <Button
                  variant="secondary"
                  size="sm"
                  asChild
                >
                  <a href={result.shortUrl} target="_blank" rel="noreferrer">
                    <ExternalLink className="h-4 w-4" /> Open
                  </a>
                </Button>
                <Button variant="outline" size="sm" onClick={() => setQrOpen(true)}>
                  <QrCode className="h-4 w-4" /> QR
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => onViewAnalytics(result.code)}
                >
                  <BarChart3 className="h-4 w-4" /> View analytics
                </Button>
              </div>
            </CardContent>
          </Card>

          <QrDialog
            open={qrOpen}
            onOpenChange={setQrOpen}
            shortUrl={result.shortUrl}
            code={result.code}
          />
        </motion.div>
      ) : null}
    </AnimatePresence>
  )
}
