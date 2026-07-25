import type { LabeledCount } from '@/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { truncate } from '@/lib/format'

type Props = {
  title: string
  description: string
  items: LabeledCount[]
  loading: boolean
  emptyLabel?: string
}

export function TopList({ title, description, items, loading, emptyLabel = 'No data yet' }: Props) {
  const max = Math.max(...items.map((i) => i.count), 1)

  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        {loading ? (
          Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="h-9 w-full" />
          ))
        ) : items.length === 0 ? (
          <p className="py-6 text-center text-sm text-[var(--color-muted-foreground)]">
            {emptyLabel}
          </p>
        ) : (
          items.map((item) => (
            <div key={item.label} className="space-y-1.5">
              <div className="flex items-center justify-between gap-3 text-sm">
                <span className="truncate font-medium" title={item.label}>
                  {truncate(item.label, 42)}
                </span>
                <span className="shrink-0 font-mono text-xs text-[var(--color-muted-foreground)]">
                  {item.count}
                </span>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-[var(--color-muted)]">
                <div
                  className="h-full rounded-full bg-gradient-to-r from-[var(--color-primary)] to-[var(--color-accent)] transition-all duration-500"
                  style={{ width: `${(item.count / max) * 100}%` }}
                />
              </div>
            </div>
          ))
        )}
      </CardContent>
    </Card>
  )
}
