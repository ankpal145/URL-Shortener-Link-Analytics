import { motion } from 'framer-motion'
import { Clock, MousePointerClick, Timer, Users } from 'lucide-react'
import type { StatsResponse } from '@/api'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { formatNumber, relativeTime } from '@/lib/format'

type Props = {
  stats: StatsResponse | null
  loading: boolean
}

const items = [
  {
    key: 'total',
    label: 'Total clicks',
    icon: MousePointerClick,
    value: (s: StatsResponse) => formatNumber(s.totalClicks),
  },
  {
    key: 'unique',
    label: 'Unique visitors',
    icon: Users,
    value: (s: StatsResponse) => formatNumber(s.uniqueVisitors),
  },
  {
    key: 'first',
    label: 'First click',
    icon: Clock,
    value: (s: StatsResponse) => relativeTime(s.firstClickAt),
  },
  {
    key: 'last',
    label: 'Last click',
    icon: Timer,
    value: (s: StatsResponse) => relativeTime(s.lastClickAt),
  },
] as const

export function KpiCards({ stats, loading }: Props) {
  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
      {items.map((item, i) => {
        const Icon = item.icon
        return (
          <motion.div
            key={item.key}
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.05 * i, duration: 0.35 }}
          >
            <Card>
              <CardContent className="flex items-start justify-between gap-3 pt-5">
                <div>
                  <p className="text-xs font-medium tracking-wide text-[var(--color-muted-foreground)] uppercase">
                    {item.label}
                  </p>
                  {loading || !stats ? (
                    <Skeleton className="mt-2 h-8 w-20" />
                  ) : (
                    <p className="mt-1 text-2xl font-bold tracking-tight">
                      {item.value(stats)}
                    </p>
                  )}
                </div>
                <div className="rounded-xl bg-[color-mix(in_oklab,var(--color-primary)_16%,transparent)] p-2.5 text-[var(--color-primary)]">
                  <Icon className="h-4 w-4" />
                </div>
              </CardContent>
            </Card>
          </motion.div>
        )
      })}
    </div>
  )
}
