import { QRCodeSVG } from 'qrcode.react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  shortUrl: string
  code: string
}

export function QrDialog({ open, onOpenChange, shortUrl, code }: Props) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>QR for /{code}</DialogTitle>
          <DialogDescription>
            Scan to open the short link on any device.
          </DialogDescription>
        </DialogHeader>
        <div className="flex flex-col items-center gap-4 py-2">
          <div className="rounded-2xl bg-white p-4 shadow-inner">
            <QRCodeSVG value={shortUrl} size={200} level="M" includeMargin />
          </div>
          <p className="max-w-full truncate font-mono text-xs text-[var(--color-muted-foreground)]">
            {shortUrl}
          </p>
        </div>
      </DialogContent>
    </Dialog>
  )
}
