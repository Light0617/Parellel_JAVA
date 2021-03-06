package edu.coursera.concurrent.prim_actor_pipelin;

import edu.rice.pcdp.Actor;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor actor = new SieveActorActor();
        finish(() -> {
            for(int i = 3; i <= limit; i += 2){
                actor.send(i);
            }
            actor.send(0);
        });
        int numPrimes = 1;
        SieveActorActor curActor = actor;
        while(curActor != null) {
            numPrimes += curActor.getNumLocalPrimes();
            curActor = curActor.getNextActor();
        }

        return numPrimes;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */
        private static final int MAX_LOCAL_PRIMES = 1000;
        private int[] localPrimes = new int[MAX_LOCAL_PRIMES];
        private int numLocalPrimes;
        private SieveActorActor nextActor;

        public SieveActorActor getNextActor() {
            return nextActor;
        }

        public int getNumLocalPrimes() {
            return numLocalPrimes;
        }


        @Override
        public void process(final Object msg) {
            final int candidate = (Integer) msg;
            if(candidate <= 0) {
                if(nextActor != null) {
                    nextActor.send(msg);
                }
                return;
            } else {
                final boolean isPrime = isLocallyPrime(candidate);
                if(!isPrime) return;
                if(numLocalPrimes < MAX_LOCAL_PRIMES){
                    localPrimes[numLocalPrimes++] = candidate;
                } else {
                    if(nextActor == null){
                        nextActor = new SieveActorActor();
                    }
                    nextActor.send(msg);
                }
            }
        }

        private boolean isLocallyPrime(int candidate) {
            for(int i = 0; i < numLocalPrimes; i++){
                if(candidate % localPrimes[i] == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
