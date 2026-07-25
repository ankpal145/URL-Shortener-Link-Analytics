import { motion } from 'framer-motion'
import { Link2, ShieldCheck, Sparkles } from 'lucide-react'

const words = ['Shorten.', 'Share.', 'Understand.']

export function Hero() {
  return (
    <section className="relative overflow-hidden pt-10 pb-6 sm:pt-16 sm:pb-10">
      <div className="aurora" aria-hidden />
      <div className="grid-fade pointer-events-none absolute inset-0" aria-hidden />

      <div className="relative mx-auto max-w-5xl px-4 text-center">
        <motion.a
          href="https://github.com/ankpal145/URL-Shortener-Link-Analytics"
          target="_blank"
          rel="noreferrer"
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.45 }}
          className="mb-6 inline-flex items-center gap-2 rounded-full border border-[var(--color-card-border)] bg-[color-mix(in_oklab,var(--color-card)_80%,transparent)] px-3 py-1.5 text-xs font-medium text-[var(--color-muted-foreground)] backdrop-blur transition hover:text-[var(--color-foreground)]"
        >
          <Sparkles className="h-3.5 w-3.5 text-[var(--color-accent)]" />
          Java 21 + Spring Boot · open source
        </motion.a>

        <h1 className="mx-auto max-w-3xl text-4xl font-extrabold tracking-tight sm:text-6xl">
          {words.map((word, i) => (
            <motion.span
              key={word}
              className={i === words.length - 1 ? 'text-gradient' : 'mr-2 sm:mr-3'}
              initial={{ opacity: 0, y: 24, filter: 'blur(6px)' }}
              animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
              transition={{ delay: 0.12 + i * 0.12, duration: 0.55, ease: [0.22, 1, 0.36, 1] }}
            >
              {word}{' '}
            </motion.span>
          ))}
        </h1>

        <motion.p
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5, duration: 0.45 }}
          className="mx-auto mt-5 max-w-2xl text-base text-[var(--color-muted-foreground)] sm:text-lg"
        >
          Turn long links into elegant short codes — then watch every click with
          privacy-conscious analytics. No raw IPs. Just signal.
        </motion.p>

        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.65, duration: 0.45 }}
          className="mt-7 flex flex-wrap items-center justify-center gap-3 text-xs text-[var(--color-muted-foreground)]"
        >
          <span className="inline-flex items-center gap-1.5 rounded-full bg-[var(--color-muted)] px-3 py-1.5">
            <Link2 className="h-3.5 w-3.5 text-[var(--color-primary)]" />
            Custom aliases
          </span>
          <span className="inline-flex items-center gap-1.5 rounded-full bg-[var(--color-muted)] px-3 py-1.5">
            <ShieldCheck className="h-3.5 w-3.5 text-[var(--color-accent)]" />
            Salted IP hashing
          </span>
        </motion.div>
      </div>
    </section>
  )
}
