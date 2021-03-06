/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.CheckLevel;

import java.util.*;

/**
 * WarningsGuard that represents just a chain of other guards. For example we
 * could have following chain
 * 1) all warnings outside of /foo/ should be suppressed
 * 2) errors with key JSC_BAR should be marked as warning
 * 3) the rest should be reported as error
 *
 * This class is designed for such behaviour.
 *
 * @author anatol@google.com (Anatol Pomazau)
 */
public class ComposeWarningsGuard extends WarningsGuard {

  private final List<WarningsGuard> guards;
  private static final Comparator<WarningsGuard> guardComparator =
      new Comparator<WarningsGuard>() {
    @Override
    public int compare(WarningsGuard a, WarningsGuard b) {
      return a.getPriority() - b.getPriority();
    }
  };

  public ComposeWarningsGuard(List<WarningsGuard> guards) {
    this.guards = Lists.newArrayList();
    addGuards(guards);
  }

  public ComposeWarningsGuard(WarningsGuard... guards) {
    this(Lists.newArrayList(guards));
  }

  void addGuard(WarningsGuard guard) {
    if (guard instanceof ComposeWarningsGuard) {
      // Reverse the guards, so that they have the same order in the result.
      addGuards(Iterables.reverse(((ComposeWarningsGuard) guard).guards));
    } else {
      int index = Collections.binarySearch(this.guards, guard, guardComparator);
      if (index < 0) {
        index = -index - 1;
      }
      this.guards.add(index, guard);
    }
  }

  private void addGuards(Iterable<WarningsGuard> guards) {
    for (WarningsGuard guard : guards) {
      addGuard(guard);
    }
  }

  @Override
  public CheckLevel level(JSError error) {
    for (WarningsGuard guard : guards) {
      CheckLevel newLevel = guard.level(error);
      if (newLevel != null) {
        return newLevel;
      }
    }
    return null;
  }

  @Override
  public boolean disables(DiagnosticGroup group) {
    nextSingleton:
    for (DiagnosticType type : group.getTypes()) {
      DiagnosticGroup singleton = DiagnosticGroup.forType(type);

      for (WarningsGuard guard : guards) {
        if (guard.disables(singleton)) {
          continue nextSingleton;
        } else if (guard.enables(singleton)) {
          return false;
        }
      }

      return false;
    }

    return true;
  }

  /**
   * Determines whether this guard will "elevate" the status of any disabled
   * diagnostic type in the group to a warning or an error.
   */
  @Override
  public boolean enables(DiagnosticGroup group) {
    for (WarningsGuard guard : guards) {
      if (guard.enables(group)) {
        return true;
      } else if (guard.disables(group)) {
        return false;
      }
    }

    return false;
  }

  List<WarningsGuard> getGuards() {
    return Collections.unmodifiableList(guards);
  }
}
