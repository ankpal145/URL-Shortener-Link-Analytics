import { Copy } from 'lucide-react'
import type { ClickEventView } from '@/api'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useToast } from '@/components/Toast'
import { formatDateTime, truncate } from '@/lib/format'

type Props = {
  rows: ClickEventView[]
  loading: boolean
}

export function RecentClicksTable({ rows, loading }: Props) {
  const { toast } = useToast()

  async function copyHash(hash: string) {
    try {
      await navigator.clipboard.writeText(hash)
      toast({ title: 'IP hash copied', variant: 'success' })
    } catch {
      toast({ title: 'Could not copy', variant: 'error' })
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Recent clicks</CardTitle>
        <CardDescription>Latest 100 events · IPs stored as salted SHA-256 only</CardDescription>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="space-y-2">
            {Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} className="h-12 w-full" />
            ))}
          </div>
        ) : rows.length === 0 ? (
          <p className="py-8 text-center text-sm text-[var(--color-muted-foreground)]">
            No clicks recorded yet. Share the short link to see activity here.
          </p>
        ) : (
          <>
            <div className="hidden overflow-x-auto md:block">
              <table className="w-full min-w-[640px] text-left text-sm">
                <thead>
                  <tr className="border-b border-[var(--color-card-border)] text-xs tracking-wide text-[var(--color-muted-foreground)] uppercase">
                    <th className="px-2 py-2 font-medium">When</th>
                    <th className="px-2 py-2 font-medium">Referer</th>
                    <th className="px-2 py-2 font-medium">User agent</th>
                    <th className="px-2 py-2 font-medium">IP hash</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row, i) => (
                    <tr
                      key={`${row.clickedAt}-${i}`}
                      className="border-b border-[var(--color-card-border)]/60 last:border-0"
                    >
                      <td className="px-2 py-3 whitespace-nowrap text-[var(--color-muted-foreground)]">
                        {formatDateTime(row.clickedAt)}
                      </td>
                      <td className="px-2 py-3" title={row.referer ?? undefined}>
                        {truncate(row.referer ?? '(direct)', 36)}
                      </td>
                      <td className="px-2 py-3" title={row.userAgent ?? undefined}>
                        {truncate(row.userAgent ?? '(unknown)', 40)}
                      </td>
                      <td className="px-2 py-3">
                        <div className="flex items-center gap-1.5">
                          <code className="font-mono text-xs">
                            {truncate(row.ipHash, 14)}
                          </code>
                          {row.ipHash ? (
                            <Button
                              type="button"
                              size="icon"
                              variant="ghost"
                              className="h-7 w-7"
                              onClick={() => copyHash(row.ipHash!)}
                              aria-label="Copy IP hash"
                            >
                              <Copy className="h-3.5 w-3.5" />
                            </Button>
                          ) : null}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="space-y-3 md:hidden">
              {rows.map((row, i) => (
                <div
                  key={`${row.clickedAt}-m-${i}`}
                  className="rounded-xl border border-[var(--color-card-border)] bg-[color-mix(in_oklab,var(--color-muted)_40%,transparent)] p-3"
                >
                  <p className="text-xs text-[var(--color-muted-foreground)]">
                    {formatDateTime(row.clickedAt)}
                  </p>
                  <p className="mt-1 text-sm font-medium">
                    {truncate(row.referer ?? '(direct)', 48)}
                  </p>
                  <p className="mt-1 text-xs text-[var(--color-muted-foreground)]" title={row.userAgent ?? undefined}>
                    {truncate(row.userAgent ?? '(unknown)', 56)}
                  </p>
                  <p className="mt-2 font-mono text-[11px] text-[var(--color-muted-foreground)]">
                    {truncate(row.ipHash, 20)}
                  </p>
                </div>
              ))}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  )
}
